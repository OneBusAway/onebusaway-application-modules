/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service.bundle.impl;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.onebusaway.admin.model.BundleBuildRequest;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.service.bundle.BundleBuildResponseDao;
import org.onebusaway.admin.service.bundle.BundleBuildingService;
import org.onebusaway.admin.util.NYCFileUtils;
import org.onebusaway.admin.util.ProcessUtil;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.container.spring.PropertyOverrideConfigurer;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreator;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.onebusaway.transit_data_federation.bundle.model.StatusMessages;
import org.onebusaway.transit_data_federation.bundle.model.TaskDefinition;
import org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementLoggerImpl;
import org.onebusaway.transit_data_federation.bundle.tasks.EntityReplacementStrategyFactory;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.StifTask;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.util.SystemTime;
import org.onebusaway.util.impl.configuration.ConfigurationServiceImpl;
import org.onebusaway.util.logging.LoggingService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.remoting.RemoteConnectFailureException;

import static org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleConventionMain.*;

public class BundleBuildingServiceImpl implements BundleBuildingService {
  private static final String BUNDLE_RESOURCE = "classpath:org/onebusaway/transit_data_federation/bundle/application-context-bundle-admin.xml";
  private static final String DEFAULT_STIF_CLEANUP_URL = "https://github.com/camsys/onebusaway-nyc/raw/master/onebusaway-nyc-stif-loader/fix-stif-date-codes.py";
  private static final String DEFAULT_AGENCY = "MTA";
  private static final String METADATA_FILENAME = "metadata.json";
  private static final String DEFAULT_TRIP_TO_DSC_FILE = "tripToDSCMap.txt";
  private static final String ARG_THROW_EXCEPTION_INVALID_STOPS = "tripEntriesFactory.throwExceptionOnInvalidStopToShapeMappingException";
  private static final String ARG_LENIENT_ARRIVAL_DEPARTURE = "stopTimeEntriesFactory.lenientArrivalDepartureTimes";

  private static Logger _log = LoggerFactory.getLogger(BundleBuildingServiceImpl.class);
  private FileService _fileService;
  private ConfigurationService configurationService;
  private LoggingService loggingService;
  private ExecutorService _executorService = null;
  private String _auxConfig = null;
  private String _stopConsolidationConfig = null;
  private boolean _debug = false;
  
  public void setDebug(boolean flag) {
    _debug = flag;
  }

  @Autowired
  private ConfigurationServiceClient configurationServiceClient;

  @Autowired
  private BundleBuildResponseDao _buildBundleResponseDao;

  @Autowired
  public void setFileService(FileService service) {
    _fileService = service;
  }

  /**
   * @param configurationService the configurationService to set
   */
   @Autowired
  public void setConfigurationService(ConfigurationService configurationService) {
	this.configurationService = configurationService;
  }

  /**
   * @param loggingService the loggingService to set
   */
  @Autowired
  public void setLoggingService(LoggingService loggingService) {
	this.loggingService = loggingService;
  }

  @Override
  public void setup() {
    _executorService = Executors.newFixedThreadPool(1);
  }
  
  @PreDestroy
  public void stop() {
    if (_executorService != null)
      _executorService.shutdownNow();
  }

  public void setAuxConfig(String flag) {
    _auxConfig = flag;
  }
  public String getAuxConfig() {
    return _auxConfig;
  }
  
  @Override
  public void doBuild(BundleBuildRequest request, BundleBuildResponse response) {
    response.setId(request.getId());
    download(request, response);
    prepare(request, response);
    build(request, response);
    assemble(request, response);
    upload(request, response);
    response.addStatusMessage("Bundle build process complete");
    String component = System.getProperty("admin.chefRole");
    loggingService.log(component, Level.INFO, "Bundle build process complete for bundle '" 
    			+request.getBundleName() + "'");
  }
  /**
   * download from S3 and stage for building 
   */
  @Override
  public void download(BundleBuildRequest request, BundleBuildResponse response) {
    String bundleDir = request.getBundleDirectory();
    String tmpDirectory = request.getTmpDirectory();
    if (tmpDirectory == null) { 
      tmpDirectory = new NYCFileUtils().createTmpDirectory();
      request.setTmpDirectory(tmpDirectory);
    }
    response.setTmpDirectory(tmpDirectory);

    // download gtfs
    List<String> gtfs = _fileService.list( bundleDir + "/" + _fileService.getGtfsPath(), -1);
	
    
    NYCFileUtils fs = new NYCFileUtils();
    
    String parentDir = _fileService.getGtfsPath(); // Parent of agency dir
    for (String file : gtfs) {
      _log.debug("downloading gtfs:" + file);
      response.addStatusMessage("downloading gtfs " + file);
      // write some meta_data into the file name for later use
      String agencyDir = parseAgencyDir(parentDir, file);
      String gtfsFileName = _fileService.get(file, tmpDirectory);
      // if we have metadata, rename file to encode metadata
      if (agencyDir != null) {
        File toRename = new File(gtfsFileName);
        String newNameStr = fs.parseDirectory(gtfsFileName) + File.separator + agencyDir
              + "_" + toRename.getName();
        try {
          fs.moveFile(gtfsFileName, newNameStr);
          response.addGtfsFile(newNameStr);
          _log.debug("gtfs file " + gtfsFileName + " renamed to " + newNameStr);
        } catch (Exception e) {
          _log.error("exception moving GTFS file:", e);
          // use the old one and carry on
          response.addGtfsFile(gtfsFileName);
        }
        
      } else {
        response.addGtfsFile(gtfsFileName);  
      }
    }
    _log.debug("finished download gtfs");
    // download aux files, which could be stif or hastus, etc
    List<String> aux = _fileService.list(
        bundleDir + "/" + _fileService.getAuxPath(), -1);
    parentDir = _fileService.getAuxPath(); // Parent of agency dir
    for (String file : aux) {
      _log.info("downloading aux:" + aux);
      response.addStatusMessage("downloading aux files " + file);
      String agencyDir = parseAgencyDir(parentDir, file);
      if (agencyDir == null) {
        response.addAuxZipFile(_fileService.get(file, tmpDirectory));
      } else {
        String auxFileName = _fileService.get(file, tmpDirectory);
        File toRename = new File(file);
        String newNameStr = fs.parseDirectory(auxFileName) + File.separator
            + agencyDir + "_" + toRename.getName();
        try {
          _log.info(auxFileName + " moved to " + newNameStr);
          fs.moveFile(auxFileName, newNameStr);
          response.addAuxZipFile(newNameStr);
        } catch (Exception e) {
          _log.error("exception moving AUX file:", e);
          // use the old one and carry on
          response.addAuxZipFile(auxFileName);
        }
     }
    }
    _log.info("finished download aux files");
    // download optional configuration files
    List<String> config = _fileService.list(
        bundleDir + "/" + _fileService.getConfigPath(), -1);
    for (String file : config) {
      _log.debug("downloading config:" + config);
      response.addStatusMessage("downloading config file " + file);
      response.addConfigFile(_fileService.get(file, tmpDirectory));
    }
    _log.debug("download complete");
    response.addStatusMessage("download complete");
  }

  /**
   * stage file locations for bundle building.
   */
  @Override
  public void prepare(BundleBuildRequest request, BundleBuildResponse response) {

	response.addStatusMessage("preparing for build");
    NYCFileUtils fs = new NYCFileUtils();
    
    // copy source data to inputs
    String rootPath = request.getTmpDirectory() + File.separator + request.getBundleName();
    response.setBundleRootDirectory(rootPath);
    File rootDir = new File(rootPath);
    rootDir.mkdirs();
    
    String inputsPath = request.getTmpDirectory() + File.separator + request.getBundleName() 
        + File.separator + INPUTS_DIR;
    response.setBundleInputDirectory(inputsPath);
    File inputsDir = new File(inputsPath);
    inputsDir.mkdirs();
    
    String outputsPath = request.getTmpDirectory() + File.separator + request.getBundleName()
        + File.separator + OUTPUT_DIR;
    response.setBundleOutputDirectory(outputsPath);
    File outputsDir = new File(outputsPath);
    outputsDir.mkdirs();

    String dataPath = request.getTmpDirectory() + File.separator + request.getBundleName()
        + File.separator + DATA_DIR;
    
    
    
    File dataDir = new File(dataPath);
    response.setBundleDataDirectory(dataPath);
    dataDir.mkdirs();
    for (String gtfs : response.getGtfsList()) {
      String outputFilename = null;
      if (!gtfs.endsWith(".zip")) {
        _log.error("ignoring gtfs path that is not a zip:" + gtfs);
        response.addStatusMessage("ignoring gtfs path that is not a zip:" + gtfs);
      } else {
        outputFilename = inputsPath + File.separator + fs.parseFileName(gtfs);
        _log.debug("prepping gtfs:" + gtfs + " to " + outputFilename);
        fs.copyFiles(new File(gtfs), new File(outputFilename));
      }
    }
    _log.debug("finished prepping gtfs!");
    
    for (String stif: response.getAuxZipList()) {
      _log.debug("prepping stif:" + stif);
      String outputFilename = inputsPath + File.separator + fs.parseFileName(stif); 
      fs.copyFiles(new File(stif), new File(outputFilename));
    }
    
    for (String stifZip : response.getAuxZipList()) {
      _log.debug("stif copying " + stifZip + " to " + request.getTmpDirectory() + File.separator
          + "stif");
      new NYCFileUtils().unzip(stifZip, request.getTmpDirectory() + File.separator
          + "stif");
    }

    _log.debug("stip unzip complete ");
    
    // stage baseLocations
    InputStream baseLocationsStream = this.getClass().getResourceAsStream("/BaseLocations.txt");
    new NYCFileUtils().copy(baseLocationsStream, dataPath + File.separator + "BaseLocations.txt");
    
    File configPath = new File(inputsPath + File.separator + "config");
    configPath.mkdirs();
    
    // stage any configuration files
    for (String config : response.getConfigList()) {
      _log.debug("config copying " + config + " to " + inputsPath + File.separator + "config");
      response.addStatusMessage("found additional configuration file=" + config);
      String outputFilename = inputsPath + File.separator + "config" + File.separator + fs.parseFileName(config);
      fs.copyFiles(new File(config), new File(outputFilename));
    }
    
    if (isStifTaskApplicable()) {
      _log.info("running stif with auxconfig=" + this._auxConfig);
      prepStif(request, response);
    } else {
      _log.info("running hastus with auxconfig=" + this._auxConfig);
      prepHastus(request, response);
    }
    
  }

   private void prepHastus(BundleBuildRequest request,
      BundleBuildResponse response) {
     NYCFileUtils fs = new NYCFileUtils();
     // create AUX dir as well
     String auxPath = request.getTmpDirectory() + File.separator + "aux";
     File auxDir = new File(auxPath);
     _log.info("creating aux directory=" + auxPath);
     auxDir.mkdirs();
  }

  //clean stifs via STIF_PYTHON_CLEANUP_SCRIPT
  private void prepStif(BundleBuildRequest request, BundleBuildResponse response) {
    
    NYCFileUtils fs = new NYCFileUtils();
    // create STIF dir as well
    String stifPath = request.getTmpDirectory() + File.separator + "stif";
    File stifDir = new File(stifPath);
    _log.info("creating stif directory=" + stifPath);
    stifDir.mkdirs();

    try {
      File[] stifDirectories = stifDir.listFiles();
      if (stifDirectories != null) {
        
        fs = new NYCFileUtils(request.getTmpDirectory());
          String stifUtilUrl = getStifCleanupUrl();
          response.addStatusMessage("downloading " + stifUtilUrl + " to clean stifs");
          fs.wget(stifUtilUrl);
          String stifUtilName = fs.parseFileName(stifUtilUrl);
          // make executable
          fs.chmod("500", request.getTmpDirectory() + File.separator + stifUtilName);

        // for each subdirectory of stif, run the script 
        for (File stifSubDir : stifDirectories) {
          String cmd = request.getTmpDirectory() + File.separator + stifUtilName + " " 
            + stifSubDir.getCanonicalPath();
          
          // kick off process and collect output
          ProcessUtil pu = new ProcessUtil(cmd);
          pu.exec();
          if (pu.getReturnCode() == null || !pu.getReturnCode().equals(0)) {
            // obanyc-1692, do not send to client
            String returnCodeMessage = stifUtilName + " exited with return code " + pu.getReturnCode();
            _log.info(returnCodeMessage);
            _log.info("output=" + pu.getOutput());
            _log.info("error=" + pu.getError());
          }
          if (pu.getException() != null) {
            response.setException(pu.getException());
          }
        }
        response.addStatusMessage("stif cleaning complete");
      } 
    } catch (Exception any) {
      response.setException(any);
    }
    
  }

  private String parseAgencyDir(String parentDir, String path) {
    Pattern pattern = Pattern.compile(parentDir + "/(.+)/");
    Matcher matcher = pattern.matcher(path);
    if (matcher.find()) {
      return matcher.group(1).replace(File.separator, "");
    }
    return null;
  }

  private String getStifCleanupUrl() {
    if (configurationService != null) {
      try {
        return configurationService.getConfigurationValueAsString("admin.stif_cleanup_url", DEFAULT_STIF_CLEANUP_URL);
      } catch (RemoteConnectFailureException e){
        _log.error("stifCleanupUrl failed:", e);
        return DEFAULT_STIF_CLEANUP_URL;
      }
    }
    return DEFAULT_STIF_CLEANUP_URL;
  }
  
  /**
   * call FederatedTransitDataBundleCreator
   */
  @Override
  public int build(BundleBuildRequest request, BundleBuildResponse response) {
    /*
     * this follows the example from FederatedTransitDataBundleCreatorMain
     */
    PrintStream stdOut = System.out;
    PrintStream logFile = null;
    
    // pass a mini spring context to the bundle builder so we can cleanup
    ConfigurableApplicationContext context = null;
    try {
      File outputPath = new File(response.getBundleDataDirectory());
      File loggingPath = new File(response.getBundleOutputDirectory());
      
      // beans assume bundlePath is set -- this will be where files are written!
      System.setProperty("bundlePath", outputPath.getAbsolutePath());
      
      String logFilename = outputPath + File.separator + "bundleBuilder.out.txt";
      logFile = new PrintStream(new FileOutputStream(new File(logFilename)));

      // swap standard out for logging
      System.setOut(logFile);
      configureLogging(System.out);
      
      FederatedTransitDataBundleCreator creator = new FederatedTransitDataBundleCreator();

      Map<String, BeanDefinition> beans = new HashMap<String, BeanDefinition>();
      creator.setContextBeans(beans);
      
      List<GtfsBundle> gtfsBundles = createGtfsBundles(response);
      List<String> contextPaths = new ArrayList<String>();
      contextPaths.add(BUNDLE_RESOURCE);
      
      BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(GtfsBundles.class);
      bean.addPropertyValue("bundles", gtfsBundles);
      beans.put("gtfs-bundles", bean.getBeanDefinition());

      // pass through configuration service so its available to tasks
      bean = BeanDefinitionBuilder.genericBeanDefinition(ConfigurationServiceImpl.class);
      beans.put("configurationServiceImpl", bean.getBeanDefinition());

      bean = BeanDefinitionBuilder.genericBeanDefinition(GtfsRelationalDaoImpl.class);
      beans.put("gtfsRelationalDaoImpl", bean.getBeanDefinition());

      BeanDefinitionBuilder multiCSVLogger = BeanDefinitionBuilder.genericBeanDefinition(MultiCSVLogger.class);
      multiCSVLogger.addPropertyValue("basePath", loggingPath);
      beans.put("multiCSVLogger", multiCSVLogger.getBeanDefinition());
      
      BeanDefinitionBuilder entityReplacementLogger = BeanDefinitionBuilder.genericBeanDefinition(EntityReplacementLoggerImpl.class);
      beans.put("entityReplacementLogger", entityReplacementLogger.getBeanDefinition());
      

      BeanDefinitionBuilder requestDef = BeanDefinitionBuilder.genericBeanDefinition(BundleRequestResponse.class);
      requestDef.addPropertyValue("request", request);
      requestDef.addPropertyValue("response", response);
      beans.put("bundleRequestResponse", requestDef.getBeanDefinition());
      
      // configure for NYC specifics
      BeanDefinitionBuilder bundle = BeanDefinitionBuilder.genericBeanDefinition(FederatedTransitDataBundle.class);
      bundle.addPropertyValue("path", outputPath);
      beans.put("bundle", bundle.getBeanDefinition());

      BeanDefinitionBuilder outputDirectoryReference = BeanDefinitionBuilder.genericBeanDefinition(String.class);
      outputDirectoryReference.addPropertyValue("", response.getBundleOutputDirectory());

      
      // TODO move this to application-context-bunlde-admin.xml and have it look for config to turn on/off
      BeanDefinitionBuilder task = null;
      if (isStifTaskApplicable()) {
        addStifTask(beans, request, response);
      }
      
      if (isStopConsolidationApplicable()) {
        addStopConsolidationMappings(beans, request, response);
      }
      

      _log.debug("setting outputPath=" + outputPath);
      creator.setOutputPath(outputPath);
      creator.setContextPaths(contextPaths);

      // manage our own overrides, as we use our own context
      Properties cmdOverrides = new Properties();
      cmdOverrides.setProperty(ARG_THROW_EXCEPTION_INVALID_STOPS, "false");
      cmdOverrides.setProperty(ARG_LENIENT_ARRIVAL_DEPARTURE,  "true");
      if (this.getStopVerificationURL() != null) {
        cmdOverrides.setProperty("stopVerificationTask.path", this.getStopVerificationURL());
        cmdOverrides.setProperty("stopVerificationDistanceTask.path", this.getStopVerificationURL());
      }
      String stopMappingUrl = getStopMappingUrl();
      if (stopMappingUrl != null) {
        cmdOverrides.setProperty("stopConsolidationFileTask.stopConsolidationUrl", stopMappingUrl);
      }
      creator.setAdditionalBeanPropertyOverrides(cmdOverrides);


      BeanDefinitionBuilder propertyOverrides = BeanDefinitionBuilder.genericBeanDefinition(PropertyOverrideConfigurer.class);
      propertyOverrides.addPropertyValue("properties", cmdOverrides);
      beans.put("myCustomPropertyOverrides",
          propertyOverrides.getBeanDefinition());

      // manage our own context to recover from exceptions
      Map<String, BeanDefinition> contextBeans = new HashMap<String, BeanDefinition>();
      contextBeans.putAll(beans);
      context = ContainerLibrary.createContext(contextPaths, contextBeans);
      creator.setContext(context);

      response.addStatusMessage("building bundle");
      monitorStatus(response, creator.getStatusMessages());
      creator.run();
      demonitorStatus();
      // If this is a rebuild of a bundle, re-use the previous bundle id.
      updateBundleId(request, response);
      response.addStatusMessage("bundle build complete");
      return 0;

    } catch (Exception e) {
      _log.error(e.toString(), e);
      response.setException(e);
      return 1;
    } catch (Throwable t) {
      _log.error(t.toString(), t);
      response.setException(new RuntimeException(t.toString()));
      return -1;
    } finally {
      if (context != null) {
        try {
          /*
           * here we cleanup the spring context so we can process follow on requests.
           */
        context.stop();
        context.close();
        } catch (Throwable t) {
          _log.error("buried context close:", t);
        }
      }
      // restore standard out
      deconfigureLogging(System.out);
      System.setOut(stdOut);
      
      if (logFile != null) {
        logFile.close();
      }
    }

  }

  private String getStopMappingUrl() {
    if (configurationService == null)
      return null;
    return configurationService.getConfigurationValueAsString("admin.stopMappingUrl", null);
  }
  
  private void monitorStatus(BundleBuildResponse response,
      StatusMessages statusMessages) {
    if (_executorService == null) {
      _executorService = Executors.newFixedThreadPool(1);
    }
    StatusMessageThread smt = new StatusMessageThread(response, statusMessages);
    _executorService.execute(smt);
  }

  private void demonitorStatus() {
    if (_executorService != null) {
      _executorService.shutdownNow();
      _executorService = null;
    }
  }
  
  // configure entity replacement strategy to consolidate stops based on configurable URL
  private void addStopConsolidationMappings(Map<String, BeanDefinition> beans,
      BundleBuildRequest request, BundleBuildResponse response) {
    String stopMappingUrl = configurationService.getConfigurationValueAsString("admin.stopMappingUrl", null);
    
    if (stopMappingUrl != null ) {
      _log.info("configuring stopConsolidation(" 
          + ", stops=" + stopMappingUrl);
      
      BeanDefinitionBuilder erFactory = BeanDefinitionBuilder.genericBeanDefinition(EntityReplacementStrategyFactory.class);
      
      Map<String, String> mappings = new HashMap<String, String>();
      mappings.put("org.onebusaway.gtfs.model.Stop", stopMappingUrl);
      erFactory.addPropertyValue("entityMappings", mappings);
      
      beans.put("entityReplacementStrategyFactory", erFactory.getBeanDefinition());
      
      BeanDefinitionBuilder er = BeanDefinitionBuilder.genericBeanDefinition();

      er.getRawBeanDefinition().setFactoryBeanName("entityReplacementStrategyFactory");
      er.getRawBeanDefinition().setFactoryMethodName("create");
      beans.put("entityReplacementStrategy", er.getBeanDefinition());
      
      response.addStatusMessage("configuration StopMappingUrl=" + stopMappingUrl);
    } else {
      _log.info("not configuring stopConsolidation(" 
           + ", stops=" + stopMappingUrl);
    }
    
  }

  private void addStifTask(Map<String, BeanDefinition> beans, BundleBuildRequest request, BundleBuildResponse response) {
    // STEP 3
    BeanDefinitionBuilder stifLoaderTask = BeanDefinitionBuilder.genericBeanDefinition(StifTask.class);
    stifLoaderTask.addPropertyValue("fallBackToStifBlocks", Boolean.TRUE);
    stifLoaderTask.addPropertyReference("logger", "multiCSVLogger");
    // TODO this is a convention, pull out into config?
    stifLoaderTask.addPropertyValue("stifPath", request.getTmpDirectory()
        + File.separator + "stif");
    String notInServiceDirString = request.getTmpDirectory()
        + File.separator + "config";
    String notInServiceFilename = notInServiceDirString +  File.separator + "NotInServiceDSCs.txt";

    _log.info("creating NotInServiceDSCs file = " + notInServiceFilename);
    new File(notInServiceDirString).mkdirs();
    new NYCFileUtils().createFile(notInServiceFilename,
        listToFile(request.getNotInServiceDSCList()));
    stifLoaderTask.addPropertyValue("notInServiceDscPath",
        notInServiceFilename);

    String dscMapPath = response.getBundleInputDirectory() + File.separator
        + "config" + File.separator + getTripToDSCFilename();
    _log.info("looking for configuration at " + dscMapPath);
    File dscMapFile = new File(dscMapPath);
    if (dscMapFile.exists()) {
      _log.info("loading tripToDSCMap at" + dscMapPath);
      response.addStatusMessage("loading tripToDSCMap at" + dscMapPath);
      stifLoaderTask.addPropertyValue("tripToDSCOverridePath", dscMapPath);
    } else {
      response.addStatusMessage(getTripToDSCFilename()
          + " not found, override ignored");
    }
    beans.put("stifLoaderTask", stifLoaderTask.getBeanDefinition());
    
    BeanDefinitionBuilder task = null;
    task = BeanDefinitionBuilder.genericBeanDefinition(TaskDefinition.class);
    task.addPropertyValue("taskName", "stifLoaderTask");
    task.addPropertyValue("afterTaskName", "check_shapes");
    task.addPropertyValue("beforeTaskName", "transit_graph");
    task.addPropertyReference("task", "stifLoaderTask");
    beans.put("stifLoaderTaskDef", task.getBeanDefinition());

    
  }

  private boolean isStifTaskApplicable() {
    boolean isStifTaskApplicable = true; // on by default for NYC
    
    if (_auxConfig == null) {
      try {
        _auxConfig = configurationService.getConfigurationValueAsString("admin.auxSupport", null);
        _log.info("auxConfig from configService=" + _auxConfig);
      } catch (Exception any) {
        _log.debug(any.toString(), any);
      }
    }    
    if (_auxConfig != null) { 
      isStifTaskApplicable =  !"true".equals(_auxConfig);
    }

    _log.info("isStifTaskApplicable=" + isStifTaskApplicable + " for auxConfig=" + _auxConfig);
    return isStifTaskApplicable;
  }

  private boolean isStopConsolidationApplicable() {
    boolean isStopConsolidationApplicable = false; // off by default for NYC
    
    if (_stopConsolidationConfig == null) {
      try {
        _stopConsolidationConfig = configurationService.getConfigurationValueAsString("admin.stopConsolidation", null);
        _log.info("auxConfig from configService=" + _stopConsolidationConfig);
      } catch (Exception any) {
        _log.debug(any.toString(), any);
      }
    }    
    if (_stopConsolidationConfig != null) { 
      isStopConsolidationApplicable =  "true".equals(_stopConsolidationConfig);
    }

    _log.debug("isStopConsolidationApplicable=" + isStopConsolidationApplicable + " for stopConsolidationConfig=" + _stopConsolidationConfig);
    return isStopConsolidationApplicable;
  }


  
  private String getTripToDSCFilename() {
    String dscFilename = null;
    try {
        dscFilename = configurationService.getConfigurationValueAsString("admin.tripToDSCFilename", DEFAULT_TRIP_TO_DSC_FILE);
        
    } catch (Exception any) {
      return DEFAULT_TRIP_TO_DSC_FILE;
    }
    if (dscFilename != null && dscFilename.length() > 0) {
      return dscFilename;
    }
    return DEFAULT_TRIP_TO_DSC_FILE;
  }

  /**
   * tear down the logger for the bundle building activity. 
   */
  private void deconfigureLogging(OutputStream os) {
    if (_debug) return;
    _log.info("deconfiguring logging");
    try {
      os.flush();
      os.close();
    } catch (Exception any) {
      _log.error("deconfigure logging failed:", any);
    }

    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    config.getRootLogger().removeAppender("bundlebuilder.out");
    ctx.updateLoggers();
  }

  /**
   * setup a logger just for the bundle building activity. 
   */
  private void configureLogging(OutputStream os) {
    if (_debug) return;
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();

    PatternLayout layout = PatternLayout.newBuilder().withPattern(PatternLayout.TTCC_CONVERSION_PATTERN).build();
    Writer writer = new OutputStreamWriter(os);
    WriterAppender wa = WriterAppender.newBuilder().setName("bundlebuilder.out").setLayout(layout).setTarget(writer).build();
    wa.start();

    // introducing log4j dependency here
    config.getRootLogger().addAppender(wa, null, null);
    config.addAppender(wa);
    ctx.updateLoggers();

    _log.info("configuring logging");

  }

  /**
   * arrange files for tar'ing into bundle format
   */
  @Override
  public void assemble(BundleBuildRequest request, BundleBuildResponse response) {
    response.addStatusMessage("compressing results");

    NYCFileUtils fs = new NYCFileUtils();
    // build BundleMetaData.json.
    // If this is a rebuild of a bundle, re-use the previous bundle id.
    String bundleId = checkPreviousBundleId(request, response);
    new BundleBuildingUtil().generateJsonMetadata(request, response, bundleId);

    String[] paths = {request.getBundleName()};
    String filename = request.getTmpDirectory() + File.separator + request.getBundleName() + ".tar.gz";
    response.setBundleTarFilename(filename);
    response.addStatusMessage("creating bundle=" + filename + " for root dir=" + request.getTmpDirectory());
    String baseDir = request.getTmpDirectory();
    fs.tarcvf(baseDir, paths, filename);
    
    // now copy inputs and outputs to root for easy access
    // inputs
    String inputsPath = request.getTmpDirectory() + File.separator + INPUTS_DIR;
    File inputsDestDir = new File(inputsPath);
    inputsDestDir.mkdir();
    File inputsDir = new File(response.getBundleInputDirectory());
    
    _log.debug("copying input");
    File[] inputFiles = inputsDir.listFiles();
    if (inputFiles != null) {
      for (File input : inputFiles) {
        _log.debug("copying " + input + " to " + inputsPath + File.separator + input.getName());
        fs.copyFiles(input, new File(inputsPath + File.separator + input.getName()));
      }
    }

    _log.debug("copying output");
    
    // outputs
    String outputsPath = request.getTmpDirectory() + File.separator + OUTPUT_DIR;
    File outputsDestDir = new File(outputsPath);
    outputsDestDir.mkdir();
    
    // copy log file to outputs
    File outputPath = new File(response.getBundleDataDirectory());
    String logFilename = outputPath + File.separator + "bundleBuilder.out.txt";
    fs.copyFiles(new File(logFilename), new File(response.getBundleOutputDirectory() + File.separator + "bundleBuilder.out.txt"));
    response.addOutputFile("bundleBuilder.out.txt");
    
    // copy the rest of the bundle content to outputs directory
    File outputsDir = new File(response.getBundleOutputDirectory());
    File[] outputFiles = outputsDir.listFiles();
    if (outputFiles != null) {
      for (File output : outputFiles) {
        response.addOutputFile(output.getName());
        fs.copyFiles(output, new File(outputsPath + File.separator + output.getName()));
      }
    }
    
  }

  // Check if there was a previous build with this path.  If there was,
  // return the bundle id from that build's metadata.json file.
  private String checkPreviousBundleId(BundleBuildRequest request, BundleBuildResponse response) {
    String basePath = _fileService.getBucketName();
    String versionString = createVersionString(request, response);
    String metadataFileName = basePath + File.separator + versionString
        + File.separator + OUTPUT_DIR + File.separator + METADATA_FILENAME;
    File metadataFile = new File(metadataFileName);
    String bundleId = null;
    if (metadataFile.exists()) {
      bundleId = BundleBuildingUtil.getBundleId(metadataFileName);
    }
    return bundleId;
  }

  // Get the previous build's bundle id and update the metadata files to use
  // the previous id rather than a newly generated one.
  private void updateBundleId(BundleBuildRequest request, BundleBuildResponse response) {
    String bundleId = checkPreviousBundleId(request,  response);
    if (bundleId != null) {
      // Update bundle id in outputs directory
      String metadataFileName = response.getBundleRootDirectory() + File.separator
          + OUTPUT_DIR + File.separator + METADATA_FILENAME;
      BundleBuildingUtil.setBundleId(metadataFileName, bundleId);
      // Update bundle id in data directory
      metadataFileName = response.getBundleRootDirectory() + File.separator
          + DATA_DIR + File.separator + METADATA_FILENAME;
      BundleBuildingUtil.setBundleId(metadataFileName, bundleId);
    }
    return;
  }

  private StringBuffer listToFile(List<String> notInServiceDSCList) {
    StringBuffer sb = new StringBuffer();
    for (String s : notInServiceDSCList) {
      sb.append(s).append("\n");
    }
    return sb;
  }

  private List<GtfsBundle> createGtfsBundles(BundleBuildResponse response) {
    List<String> gtfsList = response.getGtfsList();
    final String gtfsMsg = "constructing configuration for bundles=" + gtfsList; 
    response.addStatusMessage(gtfsMsg);
    _log.info(gtfsMsg);
    
    List<GtfsBundle> bundles = new ArrayList<GtfsBundle>(gtfsList.size());
    String defaultAgencyId = getDefaultAgencyId();
 
    response.addStatusMessage("default agency configured to be |" + defaultAgencyId + "|");
    for (String path : gtfsList) {
      GtfsBundle gtfsBundle = new GtfsBundle();
      gtfsBundle.setPath(new File(path));
      String agencySpecificId = getAgencySpecificId(path);
      if (agencySpecificId != null) {
        _log.info("using agency specific id=" + agencySpecificId);
        gtfsBundle.setDefaultAgencyId(agencySpecificId);
      } else {
        if (defaultAgencyId != null && defaultAgencyId.length() > 0) {
          final String msg = "for bundle " + path + " setting defaultAgencyId='" + defaultAgencyId + "'";
          response.addStatusMessage(msg);
          _log.info(msg);
          gtfsBundle.setDefaultAgencyId(defaultAgencyId);
        }
      }
      Map<String, String> mappings = getAgencyIdMappings(path); 
      if (mappings != null) { 
        _log.info("using agency specific mappings=" + mappings);
        gtfsBundle.setAgencyIdMappings(mappings);
      }
      bundles.add(gtfsBundle);
    }
    return bundles;
  }
  
  private String getStopVerificationURL() {
    String path = null;
    try {
      if (configurationServiceClient != null) {
        path = configurationServiceClient.getItem(null, "admin.stopVerificationUrl");
      }
    } catch (Exception e) {
      _log.error("configuration service lookup issue:", e); 
    }
    _log.info("stopVerificationPath = " + path);
    return path;
  }

  private Map<String, String> getAgencyIdMappings(String path) {
    String agencyId = parseAgencyFromPath(path);
    if (agencyId == null) return null;
    Map<String, String> map = new HashMap<String, String>();
    try {
      if (configurationServiceClient == null) {
        _log.error("misconfiguration: expecting configurationServiceClient!");
        return map;
      }
      String elementStr = configurationServiceClient.getItem("agency", agencyId);
      if (elementStr == null) {
        _log.error("expecting agency configuration for " + agencyId);
        return map;
      }
      String[] elements = elementStr.split(";");
      for (String pair : elements) {
        String[] components = pair.split(",");
        if (components.length == 2) {
          map.put(components[0], components[1]);
        }
      }
      if (map.size() == 0) {
        _log.error("Agency mapping is not configured for agencyId " + agencyId
            + ".");
      }
    } catch (Exception e) {
      _log.error("getAgencyIdMappings failed:", e);
    }
    return map;
  }

  private String parseAgencyFromPath(String path) {
    int lastSlash = path.lastIndexOf(File.separatorChar);
    if (lastSlash < 1) return null;
    int firstBar = path.indexOf("_", lastSlash);
    if (firstBar < 1) return null;
    
    return path.substring(lastSlash+1, firstBar);
  }

  private String getAgencySpecificId(String path) {
    String agencyId = parseAgencyFromPath(path);
    _log.info("getAgencySpecificId(" + path + ")=" + agencyId);
    return agencyId;
    
  }

  @Override
  public String getDefaultAgencyId() {
    String noDefaultAgency = configurationService.getConfigurationValueAsString("no_default_agency", null);
    if ("true".equalsIgnoreCase(noDefaultAgency)) return null;
    String agencyId = configurationService.getConfigurationValueAsString("admin.default_agency", DEFAULT_AGENCY);
    return agencyId;
  }

  @Override
  /**
   * push it back to S3
   */
  public void upload(BundleBuildRequest request, BundleBuildResponse response) {
    String versionString = createVersionString(request, response);
    response.setVersionString(versionString);
    response.addStatusMessage("uploading to " + versionString);
    _log.info("uploading " + response.getBundleOutputDirectory() + " to " + versionString);
    _fileService.put(versionString + File.separator + INPUTS_DIR, response.getBundleInputDirectory());
    response.setRemoteInputDirectory(versionString + File.separator + INPUTS_DIR);
    _fileService.put(versionString + File.separator + OUTPUT_DIR, response.getBundleOutputDirectory());
    response.setRemoteOutputDirectory(versionString + File.separator + OUTPUT_DIR);
    
    _fileService.put(versionString + File.separator + request.getBundleName() + ".tar.gz", 
        response.getBundleTarFilename());

    /* TODO implement delete 
     * for now we rely on cloud restart to delete volume for us, but that is lazy 
     */
  }

  private String createVersionString(BundleBuildRequest request,
      BundleBuildResponse response) {
    String bundleName = request.getBundleName();
    _log.info("createVersionString found bundleName=" + bundleName);
    if (bundleName == null || bundleName.length() == 0) {
      bundleName = "b" + SystemTime.currentTimeMillis();
    }
    return request.getBundleDirectory() + File.separator + 
        _fileService.getBuildPath() +  File.separator +
        bundleName;
  }


    private boolean isValidGtfs(ZipFile gtfs){
        String[] gtfsValidator = {"agency.txt", "trips.txt", "stops.txt",
        		"routes.txt", "calendar.txt"};
    	for(String validationStr : gtfsValidator){
    		if (gtfs.getEntry(validationStr)==null){
    			return false;
    		}
    	}
    	return true;
    }

  private class StatusMessageThread implements Runnable {
    private BundleBuildResponse response;
    private StatusMessages statusMessages;
    int lastMessageSize = 0;

    public StatusMessageThread(BundleBuildResponse response,
        StatusMessages statusMessages) {
      this.response = response;
      this.statusMessages = statusMessages;
    }

    @Override
    public void run() {

      while (!response.isComplete()) {

        int size = statusMessages.getSize();
        if (size > lastMessageSize) {
          for (int i = lastMessageSize; i < size; i++) {
            response.addStatusMessage(statusMessages.getMessages().get(i));
          }
        }
        lastMessageSize = size;
        try {
          Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  @Override
  public void createBundleBuildResponse(BundleBuildResponse bundleBuildResponse) {
    _buildBundleResponseDao.saveOrUpdate(bundleBuildResponse);
  }

  @Override
  public void updateBundleBuildResponse(BundleBuildResponse bundleBuildResponse) {
    _buildBundleResponseDao.saveOrUpdate(bundleBuildResponse);
  }

  @Override
  public BundleBuildResponse getBundleBuildResponseForId(String id) {
    return _buildBundleResponseDao.getBundleBuildResponseForId(id);
  }

  @Override
  public int getBundleBuildResponseMaxId() {
    return _buildBundleResponseDao.getBundleBuildResponseMaxId();
  }
}
