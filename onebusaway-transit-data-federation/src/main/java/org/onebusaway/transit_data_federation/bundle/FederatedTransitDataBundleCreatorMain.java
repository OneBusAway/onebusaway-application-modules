package org.onebusaway.transit_data_federation.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

/**
 * Command line tool for federated transit data bundle creator. Allows
 * {@link FederatedTransitDataBundleCreator} to configured and run from the
 * command line.
 * 
 * @author bdferris
 * @see FederatedTransitDataBundleCreator
 */
public class FederatedTransitDataBundleCreatorMain {

  private static final String ARG_SKIP_TO = "skipTo";

  private static final String ARG_ONLY = "only";

  private static final String ARG_SKIP = "skip";
  
  private static final String ARG_ONLY_IF_DNE = "onlyIfDoesNotExist";

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
      List<File> contextPaths = new ArrayList<File>();
      for (int i = 0; i < remainingArgs.length - 1; i++)
        contextPaths.add(new File(remainingArgs[i]));
      creator.setContextPaths(contextPaths);
      
      File outputPath = new File(remainingArgs[remainingArgs.length - 1]);
      
      if( commandLine.hasOption(ARG_ONLY_IF_DNE) && outputPath.exists() ) {
        System.err.println("Bundle path already exists.  Exiting...");
        System.exit(0);
      }
      
      creator.setOutputPath(outputPath);

      setStagesToSkip(commandLine, creator);

      try {
        creator.run();
      } catch (Exception ex) {
        ex.printStackTrace();
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
    options.addOption(ARG_ONLY_IF_DNE,false,"");
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
  }
}
