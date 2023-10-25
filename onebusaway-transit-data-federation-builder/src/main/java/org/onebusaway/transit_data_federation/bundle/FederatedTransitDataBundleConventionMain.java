/**
 * Copyright (C) 2022 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.transit_data_federation.bundle.model.*;
import org.onebusaway.transit_data_federation.bundle.utilities.BundleUtilties;
import org.onebusaway.transit_data_federation.bundle.utilities.NativeFileUtilities;
import org.onebusaway.transit_data_federation.bundle.utilities.JodaDateTimeAdapter;
import org.onebusaway.transit_data_federation.bundle.utilities.JodaLocalDateAdapter;
import org.onebusaway.util.git.GitRepositoryHelper;
import org.onebusaway.util.git.GitRepositoryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

/**
 * Convention Based / Zero configuration bundle build.  Place GTFS in
 * directory tree to build.
 */
public class FederatedTransitDataBundleConventionMain {

    private static Logger _log = LoggerFactory.getLogger(FederatedTransitDataBundleConventionMain.class);

    public static final String BUNDLE_METADATA_JSON = "BundleMetadata.json";
    public static final String METADATA_JSON = "metadata.json";
    public static final String INPUTS_DIR = "inputs";
    public static final String OUTPUT_DIR = "outputs";
    public static final String DATA_DIR = "data";

    private ObjectMapper mapper = new ObjectMapper();
    private Date toDate = null;
    private Date fromDate = null;


    public String run(String[] args) {
        long start = System.currentTimeMillis();
        logVersion();

        if (args == null || args.length != 3) {
            throw new IllegalStateException("Expecting input_directory_tree, output_directory, bundle_name");
        }

        // setup logging
        Configurator.setRootLevel(Level.INFO);
        String inputPathStr = "/tmp/input";
        String outputPathStr = "/tmp/bundle";
        String bundleName = "not_specified";

        if (args != null) {
            if (args.length > 0) {
                inputPathStr = args[0];
            }
            if (args.length > 1) {
                outputPathStr = args[1] + File.separator;
            }
            if (args.length > 2) {
                bundleName = args[2];
                outputPathStr = outputPathStr + bundleName;
            }
        }

        File outputDir = new File(outputPathStr);
        if (outputDir.exists()) {
            throw new IllegalStateException("Directory " + outputPathStr
                    + " already exists.  Please remove it.");
        }
        outputDir.mkdirs();

        BundleRequest request = createRequest(handleClassPath(inputPathStr), outputPathStr, bundleName);

        FederatedTransitDataBundleCreator creator = setup(request);
        build(creator);
        String bundleFile = assemble(request, bundleName);
        System.out.println("bundle file stored at " + bundleFile + " in " + (System.currentTimeMillis() - start)/1000 + "s");
        return bundleFile;
    }

    private String handleClassPath(String inputPathStr) {
        if (inputPathStr == null) throw new NullPointerException("inputPathStr cannot be null");
        if (inputPathStr.startsWith("classpath:")) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(inputPathStr.replace("classpath:", ""));
            if (url == null) {
                throw new RuntimeException("path not found=" + inputPathStr);
            }
            try {
                return url.toURI().toString().replace("file:","");
            } catch (URISyntaxException e) {
                throw new RuntimeException("unexpected input path=" + inputPathStr);
            }
        }

        return inputPathStr;
    }

    private BundleRequest createRequest(String inputPathStr, String outputPathStr, String bundleName) {
        BundleRequest request = new BundleRequest(inputPathStr, outputPathStr);
        request.setDataset("dataset");
        request.setName(bundleName);

        return request;
    }


    private String assemble(BundleRequest request, String bundleName) {

        _log.info("creating metadata files...");
        generateJsonBundleMetadata(request);
        generateJsonMetadata(request);
        _log.info("created metadata files!");

        NativeFileUtilities fs = new NativeFileUtilities();
        String baseDir = request.getOutput().replace(File.separator + bundleName, "");
        String[] paths = {request.getName()};
        String filename = request.getOutput()
                + File.separator + request.getName() + ".tar.gz";
        // the bundle tar itself
        fs.tarcvf(baseDir, paths, filename);

        // inputs are copied as part of setup

        // outputs
        String outputsPath = request.getBundleOutputDirectory();
        File outputsDestDir = new File(outputsPath);
        outputsDestDir.mkdir();

        // copy log file to outputs
        File outputPath = new File(request.getBundleDataDirectory());
        String logFilename = outputPath + File.separator + "bundleBuilder.out.txt";
        fs.copyFiles(new File(logFilename), new File(request.getBundleOutputDirectory() + File.separator + "bundleBuilder.out.txt"));

        // copy the rest of the bundle content to outputs directory
        File outputsDir = new File(request.getBundleOutputDirectory());
        File[] outputFiles = outputsDir.listFiles();
        if (outputFiles != null) {
            for (File output : outputFiles) {
                fs.copyFiles(output, new File(outputsPath + File.separator + output.getName()));
            }
        }

        String indexFile = System.getProperty("indexFile");
        if (indexFile != null) {
            try {
                _log.info("writing indexFile {}", indexFile);
                String indexText = "{\"latest\":\"file://" + filename + "\"}";
                FileWriter fw = new FileWriter(indexFile);
                fw.write(indexText);
                fw.close();
            } catch (Throwable t) {
                _log.error("unable to create index {}", indexFile, t);
            }
        } else {
            _log.info("indexFile not requested");
        }
        return filename;
    }

    private void build(FederatedTransitDataBundleCreator creator) {
        try {
            creator.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnknownTaskException e) {
            throw new RuntimeException(e);
        }

    }

    private FederatedTransitDataBundleCreator setup(BundleRequest request) {

        FederatedTransitDataBundleCreator creator = new FederatedTransitDataBundleCreator();

        Map<String, BeanDefinition> beans = new HashMap<String, BeanDefinition>();
        creator.setContextBeans(beans);

        // copy source GTFS to inputs for forensics
        // getInput -> getBundleInputDirectory()
        NativeFileUtilities fs = new NativeFileUtilities();
        String inputsPath = request.getInput();
        File inputsDestDir = new File(request.getInputDirectory());
        inputsDestDir.mkdir();
        File inputsDir = new File(inputsPath);

        File[] inputFiles = inputsDir.listFiles();
        if (inputFiles != null) {
            for (File input : inputFiles) {
                System.out.println("copying " + input + " to " + inputsDestDir);
                fs.copyFiles(input, inputsDestDir);
            }
        }

        File dataDestDir = new File(request.getBundleDataDirectory());
        dataDestDir.mkdir();

        File outputDestDir = new File(request.getBundleOutputDirectory());
        outputDestDir.mkdir();

        List<GtfsBundle> gtfsBundles = new ArrayList<>();
        List<String> directories;
        // pull from the source dir as it maintains the folder structure
        String directoryName = request.getInput();

        directories = listFromPath(directoryName);

        if (directories == null || directories.isEmpty())
            throw new IllegalStateException("no gtfs found for path " + directoryName
                    + " and source path" + inputsDir);

        boolean found = false;
        for (String directory: directories) {
            for (String gtfsFile: listFiles(directoryName + File.separator + directory)) {
                String agencyName = parseAgency(gtfsFile);
                found = true;
                System.out.println("found gtfs at " + gtfsFile);
                GtfsBundle gtfsBundle = new GtfsBundle();
                gtfsBundle.setPath(new File(gtfsFile));
                _log.info("setting default agency id to {} for {} ", agencyName, directoryName + File.separator + directory);
                gtfsBundle.setDefaultAgencyId(agencyName);
                gtfsBundles.add(gtfsBundle);
            }
        }

        if (!found) {
            throw new IllegalStateException("no gtfs found at " + directoryName);
        }

        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(GtfsBundles.class);
        bean.addPropertyValue("bundles", gtfsBundles);
        beans.put("gtfs-bundles", bean.getBeanDefinition());
        creator.setOutputPath(dataDestDir);
        BeanDefinitionBuilder gtfsBean = BeanDefinitionBuilder.genericBeanDefinition(GtfsRelationalDaoImpl.class);
        beans.put("gtfsRelationalDaoImpl", gtfsBean.getBeanDefinition());
        Properties propertyOverrides = new Properties();
        propertyOverrides.setProperty("bundle.path", request.getBundleDataDirectory());
        creator.setAdditionalBeanPropertyOverrides(propertyOverrides);

        return creator;


    }

    private void generateJsonMetadata(BundleRequest request) {
        try {
            BundleMetadata data = new BundleMetadata();
            String outputDirectory = request.getOutput();
            String sourceDirectory = request.getBundleOutputDirectory();

            data.setId(request.getName());
            data.setName(request.getName());
            data.setServiceDateFrom(createFromDate());
            data.setServiceDateTo(createToDate());
            data.setOutputFiles(getBundleFilesWithSumsForDirectory(
                    new File(outputDirectory),
                    new File(request.getBundleDataDirectory()),
                    null));
            data.setSourceData(getSourceFilesWithSumsForDirectory(
                    new File(outputDirectory),
                    new File(request.getBundleOutputDirectory()),
                    null));
            String outputFile = request.getBundleDataDirectory() + File.separator + METADATA_JSON;
            _log.info("writing metadata file {}", outputFile);
            mapper.writeValue(new File(outputFile), data);

            outputFile = sourceDirectory + File.separator + METADATA_JSON;
            mapper.writeValue(new File(outputFile), data);
        } catch (Exception any) {
            _log.error("exception serializing metadata.json: ", any);
        }
    }

    private List<SourceFile> getSourceFilesWithSumsForDirectory(File baseDir, File dir, File rootDir) {
        return new BundleUtilties().getSourceFilesWithSumsForDirectory(baseDir, dir, rootDir);
    }

    private List<BundleFile> getBundleFilesWithSumsForDirectory(File baseDir, File dir, File rootDir) {
        return new BundleUtilties().getBundleFilesWithSumsForDirectory(baseDir, dir, rootDir);
    }



    private Date createToDate() {
        if (toDate == null) {
            // by default we go out 30 days
            toDate = DateUtils.addDays(new Date(), 30);
        }
        return toDate;
    }

    private Date createFromDate() {
        if (fromDate == null) {
            // by default the bundle is active as of now
            fromDate = new Date();
        }
        return fromDate;
    }

    private void generateJsonBundleMetadata(BundleRequest request) {
        File bundleDir = new File(request.getInput());
        List<BundleFile> files = new BundleUtilties().getBundleFilesWithSumsForDirectory(bundleDir, bundleDir, bundleDir);
        Bundle bundle = new Bundle();
        bundle.setId(request.getName());
        bundle.setDataset(request.getDataset());
        bundle.setName(request.getName());
        bundle.setServiceDateFrom(createFromLocalDate());
        bundle.setServiceDateTo(createToLocalDate());
        DateTime now = new DateTime();
        List<String> applicableAgencyIds = new ArrayList<>();
        bundle.setApplicableAgencyIds(applicableAgencyIds);
        bundle.setFiles(files);

        String output = getGson().toJson(bundle);

        String outputFilename = request.getOutput() + File.separator + BUNDLE_METADATA_JSON;
        File outputFile = new File(outputFilename);
        _log.info("creating metadata file=" + outputFilename);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outputFile);
            writer.print(output);
        } catch (Exception any) {
            _log.error(any.toString(), any);
        } finally {
            writer.close();
        }

    }

    private Gson getGson() {
        return new GsonBuilder().serializeNulls()
                .registerTypeAdapter(DateTime.class, new JodaDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new JodaLocalDateAdapter())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).setPrettyPrinting().create();

    }

    private LocalDate createToLocalDate() {
        return new LocalDate(createToDate());
    }

    private LocalDate createFromLocalDate() {
        return new LocalDate(createFromDate());
    }

    private List<String> listFiles(String directory) {
        ArrayList<String> list = new ArrayList<>();
        if (directory == null) {
            System.out.println("illegal dir=" + directory);
        }
        File dir = new File(directory);
        File[] listFiles = dir.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile()) {
                    list.add(sanitize(file.toURI().toString()));
                } else if (file.isDirectory()) {
                    List<String> recurse = listFiles(file.toString());
                    if (recurse != null && !recurse.isEmpty()) {
                        list.addAll(recurse);
                    }
                }
            }
        }
        return list;
    }

    private String sanitize(String s) {
        return s.replace("file:", "");
    }

    private String parseAgency(String directory) {
        String[] paths = directory.split("/");
        int lastIndex = paths.length - 2;
        return paths[lastIndex];
    }

    private List<String> listFromPath(String directoryName) {
        File dir = new File(directoryName);
        if (!dir.exists() || !dir.isDirectory())
            throw new RuntimeException("expecting directory at " + directoryName);
        List<String> filenames = new ArrayList<>();
        for (String s : dir.list()) {
            filenames.add(s);
        }
        return filenames;
    }

    private List<String> listFromClasspath(String directoryName) throws URISyntaxException {
        directoryName = directoryName.replace("classpath:", "");
        List<String> filenames = new ArrayList<>();

        URL url = Thread.currentThread().getContextClassLoader().getResource(directoryName);
        if (url != null) {
            if (url.getProtocol().equals("file")) {
                File file = Paths.get(url.toURI()).toFile();
                if (file != null) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File filename : files) {
                            filenames.add(filename.toString());
                        }
                    }
                }
            } else {
                System.out.println("unexpected path " + url);
            }
        }
        return filenames;
    }

    private static class BundleRequest {

        private String input;
        private String output;
        private String dataset;
        private String name;

        public BundleRequest(String inputPathStr, String outputPathStr) {
            this.input = inputPathStr;
            this.output = outputPathStr;
        }

        public String getInput() {
            return input;
        }
        public String getOutput() {
            return output;
        }
        public String getDataset() {
            return dataset;
        }
        public void setDataset(String dataset) {
            this.dataset = dataset;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public String getTmpDirectory() {
            return System.getProperty("java.io.tmpdir");
        }

        public String getInputDirectory() {
            // construct input directory
            return output + File.separator + INPUTS_DIR;
        }

        public String getBundleDataDirectory() {
            // construct data directory
            return output + File.separator + DATA_DIR;
        }

        public String getBundleOutputDirectory() {
            // construct output directory
            return output + File.separator + OUTPUT_DIR;
        }
    }

    private void logVersion() {
        GitRepositoryState gitRepositoryState = new GitRepositoryHelper().getGitRepositoryState();
        if (gitRepositoryState != null) {
            final String msg = "onebusaway-application-modules launch with branch '"
            + gitRepositoryState.getBranch()
            + "' commit '"
            + gitRepositoryState.getCommitId()
            + "' version/commit message '"
            + gitRepositoryState.getCommitMessageShort() + "'";
            _log.info(msg);
            System.out.println(msg);
        } else {
            final String msg = "onebusaway-application-modules launch running in development mode";
            _log.info(msg);
            System.out.println(msg);
        }
    }

    public static void main(String[] args) throws Exception {
        FederatedTransitDataBundleConventionMain main = new FederatedTransitDataBundleConventionMain();
        main.run(args);
    }

}
