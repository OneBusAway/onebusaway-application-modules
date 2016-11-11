/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.onebusaway.admin.model.BundleRequestResponse;
import org.onebusaway.admin.service.bundle.BundleValidationService;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Run the python transitfeed validator against the output GTFS to verify we are not
 * generating invalid GTFS.  Parse errors results into CSV, and provide full HTML
 * results as well.
 * @author jpearson
 *
 */
public class GtfsFullValidationTask implements  Runnable {
  private static Logger _log = LoggerFactory.getLogger(GtfsFullValidationTask.class);
  protected ApplicationContext _applicationContext;

  // lazy instantiated below
  private ExecutorService _executor = null;
  
  private BundleRequestResponse requestResponse;
  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    _applicationContext = applicationContext;
  }

  @Autowired
  public void setBundleRequestResponse(BundleRequestResponse requestResponse) {
    this.requestResponse = requestResponse;
  }

  @Autowired
  private BundleValidationService _validateService;

  @Autowired
  protected MultiCSVLogger _logger;

  public void setValidateService(BundleValidationService validateService) {
    _validateService = validateService;
  }

  public void setLogger(MultiCSVLogger logger) {
    _logger = logger;
  }

  @Override
  public void run() {
    _log.info("GtfsFullValidationTask Starting");
    // Only run  this on a Final build
    if (!requestResponse.getRequest().getArchiveFlag()) {
      _log.info("archive flag not set, GtfsFullValidationTask Exiting");
      return;
    }

    processGtfsBundles(getGtfsBundles(_applicationContext));
    
    _log.info("GtfsFullValidationTask Exiting");
  }

  private void processGtfsBundles(GtfsBundles gtfsBundles) {
    List<GtfsFullValidationTaskJobResult> results = new ArrayList<GtfsFullValidationTaskJobResult>();
    
    for (GtfsBundle gtfsBundle : gtfsBundles.getBundles()) {
      
      try {
        results.add(submitJob(_validateService, gtfsBundle));
      } catch (Exception any) {
        _log.error("GtfsFullValidationTask failed for gtfsBundle:" 
            + gtfsBundle.getPath().getName(), any);
      }
    }

    try {
      // give the executor a chance to run
      Thread.sleep(1 * 1000);
    } catch (InterruptedException e1) {
      return;
    }
    
    // here we wait on the tasks to finish up
    
    int i = 0;
    for (GtfsFullValidationTaskJobResult result : results) {
      while (!result.isDone()) {
        try {
          _log.info("waiting on thread[" + i + "/"+ results.size() + "] " + result.getCsvFileName());
          Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
          return;
        }
      }
      i++;
      _log.info("result " + result.getCsvFileName() + " completed in " + result.getRunTime()/1000 + "s");
      // process the results in the order they were submitted
      processResult(result);

    }
  }

  private void processResult(GtfsFullValidationTaskJobResult result) {
    _logger.header(result.getCsvFileName(), result.getColumnName());
    for (String msg : result.getErrors()) {
      _logger.logCSV(result.getCsvFileName(), msg);
    }
  }

  private GtfsFullValidationTaskJobResult submitJob(BundleValidationService validateService, GtfsBundle gtfsBundle) {
    String agencyId = gtfsBundle.getDefaultAgencyId();
    String csvFileName = agencyId + "_gtfs_validation_errors.csv";
    GtfsFullValidationTaskJobResult result = new GtfsFullValidationTaskJobResult(csvFileName, "Error Message, Error Detail");
    String outputFile = requestResponse.getResponse().getBundleOutputDirectory() 
        + "/" + gtfsBundle.getPath().getName() + ".html";
    GtfsFullValidationTaskJob wt = new GtfsFullValidationTaskJob(validateService, gtfsBundle, outputFile, result);
    // submit job for processing
    _log.info("submitting job " + gtfsBundle.getPath().getName());
    getExecutorService().submit(wt);
    // this adds it to summary.csv so the html result file can be viewed
    _logger.header(gtfsBundle.getPath().getName() + ".html", "", ""); 
    return result;
  }


  private ExecutorService getExecutorService() {
    if (_executor == null) {
      int cpus = Runtime.getRuntime().availableProcessors();
      _executor = Executors.newFixedThreadPool(cpus);
      _log.info("created threadpool of " + cpus);
    }
    return _executor;
  }
  
  protected GtfsBundles getGtfsBundles(ApplicationContext context) {

    GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");
    if (bundles != null)
      return bundles;

    GtfsBundle bundle = (GtfsBundle) context.getBean("gtfs-bundle");
    if (bundle != null) {
      bundles = new GtfsBundles();
      bundles.getBundles().add(bundle);
      return bundles;
    }

    throw new IllegalStateException(
        "must define either \"gtfs-bundles\" or \"gtfs-bundle\" in config");
  }


  
  
}
