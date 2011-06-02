package org.onebusaway.transit_data_federation.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.opentripplanner.graph_builder.impl.osm.FileBasedOpenStreetMapProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

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

  private static final String ARG_BUNDLE_KEY = "bundleKey";

  private static final String ARG_RANDOMIZE_CACHE_DIR = "randomizeCacheDir";

  private static final String ARG_ADDITIONAL_RESOURCES_DIRECTORY = "additionalResourcesDirectory";

  private static final String ARG_OSM = "osm";

  public static void main(String[] args) throws IOException,
      ClassNotFoundException {
    FederatedTransitDataBundleCreatorMain main = new FederatedTransitDataBundleCreatorMain();
    main.run(args);
  }

  public void run(String[] args) {

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
      List<File> contextPaths = new ArrayList<File>();

      for (int i = 0; i < remainingArgs.length - 1; i++) {
        File path = new File(remainingArgs[i]);
        if (path.isDirectory() || path.getName().endsWith(".zip")) {
          GtfsBundle gtfsBundle = new GtfsBundle();
          gtfsBundle.setPath(path);
          gtfsBundles.add(gtfsBundle);
        } else {
          contextPaths.add(path);
        }
      }

      if (!gtfsBundles.isEmpty()) {
        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(GtfsBundles.class);
        bean.addPropertyValue("bundles", gtfsBundles);
        beans.put("gtfs-bundles", bean.getBeanDefinition());
      }

      creator.setContextPaths(contextPaths);

      if (commandLine.hasOption(ARG_OSM)) {
        File osmPath = new File(commandLine.getOptionValue(ARG_OSM));
        BeanDefinitionBuilder bean = BeanDefinitionBuilder.genericBeanDefinition(FileBasedOpenStreetMapProviderImpl.class);
        bean.addPropertyValue("path", osmPath);
        beans.put("osmProvider", bean.getBeanDefinition());
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

      creator.setOutputPath(outputPath);

      setStagesToSkip(commandLine, creator);

      try {

        if (commandLine.hasOption(ARG_ADDITIONAL_RESOURCES_DIRECTORY)) {
          File additionalResourceDirectory = new File(
              commandLine.getOptionValue(ARG_ADDITIONAL_RESOURCES_DIRECTORY));
          copyFiles(additionalResourceDirectory, outputPath);
        }

        creator.run();
      } catch (Exception ex) {
        _log.error("error building transit data bundle",ex);
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
    options.addOption(ARG_SKIP_TO, true, "");
    options.addOption(ARG_ONLY, true, "");
    options.addOption(ARG_SKIP, true, "");
    options.addOption(ARG_INCLUDE, true, "");
    options.addOption(ARG_ONLY_IF_DNE, false, "");
    options.addOption(ARG_BUNDLE_KEY, true, "");
    options.addOption(ARG_RANDOMIZE_CACHE_DIR, false, "");
    options.addOption(ARG_ADDITIONAL_RESOURCES_DIRECTORY, true, "");
    options.addOption(ARG_OSM, true, "");

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
    System.err.println("usage: bundle-context.xml output_directory");
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
