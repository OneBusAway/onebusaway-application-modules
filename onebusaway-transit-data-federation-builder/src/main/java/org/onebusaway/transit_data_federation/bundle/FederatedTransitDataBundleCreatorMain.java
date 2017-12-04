/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Command line tool for federated transit data bundle creator. Allows
 * {@link FederatedTransitDataBundleCreator} to configured and run from the
 * command line.
 * 
 * @author bdferris
 * @see FederatedTransitDataBundleCreator
 */
public class FederatedTransitDataBundleCreatorMain {

  private static final Logger _log = LoggerFactory.getLogger(FederatedTransitDataBundleCreatorMain.class);

  private static final String ARG_SKIP_TO = "skipTo";

  private static final String ARG_ONLY = "only";

  private static final String ARG_SKIP = "skip";

  private static final String ARG_INCLUDE = "include";

  private static final String ARG_ONLY_IF_DNE = "onlyIfDoesNotExist";

  private static final String ARG_USE_DATABASE_FOR_GTFS = "useDatabaseForGtfs";

  private static final String ARG_DATASOURCE_DRIVER_CLASS_NAME = "dataSourceDriverClassName";

  private static final String ARG_DATASOURCE_URL = "dataSourceUrl";

  private static final String ARG_DATASOURCE_USERNAME = "dataSourceUsername";

  private static final String ARG_DATASOURCE_PASSWORD = "dataSourcePassword";

  private static final String ARG_BUNDLE_KEY = "bundleKey";

  private static final String ARG_RANDOMIZE_CACHE_DIR = "randomizeCacheDir";

  private static final String ARG_ADDITIONAL_RESOURCES_DIRECTORY = "additionalResourcesDirectory";

  public static void main(String[] args) throws Exception {
    System.out.println(System.getProperties());
    FederatedTransitDataBundleCreatorMain main = new FederatedTransitDataBundleCreatorMain();
    main.run(args);
  }

  public void run(String[] args) throws Exception {

    try {
      Parser parser = new GnuParser();

      Options options = new Options();
      buildOptions(options);

      CommandLine commandLine = parser.parse(options, args);

      String[] remainingArgs = commandLine.getArgs();

      if (remainingArgs.length < 2) {
        printUsage();
        System.exit(-1);
      }

      FederatedTransitDataBundleCreator creator = new FederatedTransitDataBundleCreator();

      Map<String, BeanDefinition> beans = new HashMap<String, BeanDefinition>();
      creator.setContextBeans(beans);

      List<GtfsBundle> gtfsBundles = new ArrayList<GtfsBundle>();
      List<String> contextPaths = new ArrayList<String>();

      for (int i = 0; i < remainingArgs.length - 1; i++) {
        File path = new File(remainingArgs[i]);
        if (path.isDirectory() || path.getName().endsWith(".zip")) {
          GtfsBundle gtfsBundle = new GtfsBundle();
          gtfsBundle.setPath(path);
          gtfsBundles.add(gtfsBundle);
        } else {
          contextPaths.add("file:" + path);
        }
      }

      if (!gtfsBundles.isEmpty()) {
        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(GtfsBundles.class);
        bean.addPropertyValue("bundles", gtfsBundles);
        beans.put("gtfs-bundles", bean.getBeanDefinition());
      }

      if (commandLine.hasOption(ARG_USE_DATABASE_FOR_GTFS)) {
        contextPaths.add("classpath:org/onebusaway/gtfs/application-context.xml");
      } else {
        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(GtfsRelationalDaoImpl.class);
        beans.put("gtfsRelationalDaoImpl", bean.getBeanDefinition());
      }

      if (commandLine.hasOption(ARG_DATASOURCE_URL)) {
        String dataSourceUrl = commandLine.getOptionValue(ARG_DATASOURCE_URL);
        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(DriverManagerDataSource.class);
        bean.addPropertyValue("url", dataSourceUrl);
        if (commandLine.hasOption(ARG_DATASOURCE_DRIVER_CLASS_NAME))
          bean.addPropertyValue("driverClassName",
              commandLine.getOptionValue(ARG_DATASOURCE_DRIVER_CLASS_NAME));
        if (commandLine.hasOption(ARG_DATASOURCE_USERNAME))
          bean.addPropertyValue("username",
              commandLine.getOptionValue(ARG_DATASOURCE_USERNAME));
        if (commandLine.hasOption(ARG_DATASOURCE_PASSWORD))
          bean.addPropertyValue("password",
              commandLine.getOptionValue(ARG_DATASOURCE_PASSWORD));
        beans.put("dataSource", bean.getBeanDefinition());
      }


      File outputPath = new File(remainingArgs[remainingArgs.length - 1]);

      if (commandLine.hasOption(ARG_ONLY_IF_DNE) && outputPath.exists()) {
        System.err.println("Bundle path already exists.  Exiting...");
        System.exit(0);
      }

      if (commandLine.hasOption(ARG_RANDOMIZE_CACHE_DIR))
        creator.setRandomizeCacheDir(true);

      if (commandLine.hasOption(ARG_BUNDLE_KEY)) {
        String key = commandLine.getOptionValue(ARG_BUNDLE_KEY);
        creator.setBundleKey(key);
      }

      /**
       * Optionally override any system properties (ok this duplicates existing
       * functionality, yes, but it allows for -D arguments after the main
       * class)
       */
      if (commandLine.hasOption("D")) {
        Properties props = commandLine.getOptionProperties("D");
        for (Object key : props.keySet()) {
          String propName = (String) key;
          String propValue = props.getProperty(propName);
          System.setProperty(propName, propValue);
        }
      }

      /**
       * Optionally override any system properties (ok this duplicates existing
       * functionality, yes, but it allows for -D arguments after the main
       * class)
       */
      if (commandLine.hasOption("P")) {
        Properties props = commandLine.getOptionProperties("P");
        creator.setAdditionalBeanPropertyOverrides(props);
      }

      setStagesToSkip(commandLine, creator);

      creator.setOutputPath(outputPath);
      creator.setContextPaths(contextPaths);

      try {

        if (commandLine.hasOption(ARG_ADDITIONAL_RESOURCES_DIRECTORY)) {
          File additionalResourceDirectory = new File(
              commandLine.getOptionValue(ARG_ADDITIONAL_RESOURCES_DIRECTORY));
          copyFiles(additionalResourceDirectory, outputPath);
        }

        creator.run();
      } catch (Exception ex) {
        _log.error("error building transit data bundle", ex);
        System.exit(-1);
      }
    } catch (ParseException ex) {
      System.err.println(ex.getLocalizedMessage());
      printUsage();
      System.exit(-1);
    }

    System.exit(0);
  }

  protected void buildOptions(Options options) {
    options.addOption(ARG_USE_DATABASE_FOR_GTFS, false, "");
    options.addOption(ARG_SKIP_TO, true, "");
    options.addOption(ARG_ONLY, true, "");
    options.addOption(ARG_SKIP, true, "");
    options.addOption(ARG_INCLUDE, true, "");
    options.addOption(ARG_ONLY_IF_DNE, false, "");
    options.addOption(ARG_DATASOURCE_DRIVER_CLASS_NAME, true, "");
    options.addOption(ARG_DATASOURCE_URL, true, "");
    options.addOption(ARG_DATASOURCE_USERNAME, true, "");
    options.addOption(ARG_DATASOURCE_PASSWORD, true, "");
    options.addOption(ARG_BUNDLE_KEY, true, "");
    options.addOption(ARG_RANDOMIZE_CACHE_DIR, false, "");
    options.addOption(ARG_ADDITIONAL_RESOURCES_DIRECTORY, true, "");

    Option dOption = new Option("D", "use value for given property");
    dOption.setArgName("property=value");
    dOption.setArgs(2);
    dOption.setValueSeparator('=');
    options.addOption(dOption);

    Option pOption = new Option("P", "use value for given property");
    pOption.setArgName("beanName.beanProperty=value");
    pOption.setArgs(2);
    pOption.setValueSeparator('=');
    options.addOption(pOption);
  }

  protected void printUsage() {
    InputStream is = getClass().getResourceAsStream("usage.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        System.err.println(line);
      }
    } catch (IOException ex) {

    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {

        }
      }
    }
  }

  protected void setStagesToSkip(CommandLine commandLine,
      FederatedTransitDataBundleCreator creator) {

    if (commandLine.hasOption(ARG_SKIP_TO)) {
      String value = commandLine.getOptionValue(ARG_SKIP_TO);
      creator.setSkipToTask(value);
    }

    if (commandLine.hasOption(ARG_ONLY)) {
      String[] values = commandLine.getOptionValues(ARG_ONLY);
      for (String value : values)
        creator.addTaskToOnlyRun(value);
    }

    if (commandLine.hasOption(ARG_SKIP)) {
      String[] values = commandLine.getOptionValues(ARG_SKIP);
      for (String value : values)
        creator.addTaskToSkip(value);
    }

    if (commandLine.hasOption(ARG_INCLUDE)) {
      String[] values = commandLine.getOptionValues(ARG_INCLUDE);
      for (String value : values)
        creator.addTaskToInclude(value);
    }
  }

  protected void copyFiles(File from, File to) throws IOException {

    if (!from.exists())
      return;

    if (from.isDirectory()) {
      to.mkdirs();
      for (File fromChild : from.listFiles()) {
        File toChild = new File(to, fromChild.getName());
        copyFiles(fromChild, toChild);
      }
    } else {
      FileInputStream in = null;
      FileOutputStream out = null;

      try {
        in = new FileInputStream(from);
        out = new FileOutputStream(to);

        int byteCount = 0;
        byte[] buffer = new byte[1024];
        while ((byteCount = in.read(buffer)) >= 0)
          out.write(buffer, 0, byteCount);
      } finally {
        if (in != null)
          in.close();
        if (out != null)
          out.close();
      }

    }
  }
}
