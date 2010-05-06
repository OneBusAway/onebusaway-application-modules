package org.onebusaway.transit_data_federation.bundle;

import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreator.Stages;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FederatedTransitDataBundleCreatorMain {

  private static final String ARG_SKIP_TO = "skipTo";

  private static final String ARG_ONLY = "only";

  private static final String ARG_SKIP = "skip";

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

      if (remainingArgs.length != 2) {
        printUsage();
        System.exit(-1);
      }

      FederatedTransitDataBundleCreator creator = new FederatedTransitDataBundleCreator();
      creator.setContextPath(new File(remainingArgs[0]));
      creator.setOutputPath(new File(remainingArgs[1]));

      setStagesToSkip(commandLine, creator);

      try {
        creator.run();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } catch (ParseException ex) {
      System.err.println(ex.getLocalizedMessage());
      printUsage();
      System.exit(-1);
    }

  }

  protected void buildOptions(Options options) {
    options.addOption(ARG_SKIP_TO, true, "");
    options.addOption(ARG_ONLY, true, "");
    options.addOption(ARG_SKIP, true, "");
  }

  protected void printUsage() {

  }

  protected void setStagesToSkip(CommandLine commandLine,
      FederatedTransitDataBundleCreator creator) {

    if (commandLine.hasOption(ARG_SKIP_TO)) {
      String value = commandLine.getOptionValue(ARG_SKIP_TO);
      Stages stage = FederatedTransitDataBundleCreator.Stages.valueOf(value);
      Stages[] stages = FederatedTransitDataBundleCreator.Stages.values();
      for (Stages s : stages) {
        if (s.equals(stage))
          break;
        creator.setStageToSkip(s);
      }
    }

    if (commandLine.hasOption(ARG_ONLY)) {
      String[] values = commandLine.getOptionValues(ARG_ONLY);
      Set<Stages> stages = new HashSet<Stages>();
      for (String value : values)
        stages.add(Stages.valueOf(value));
      for (Stages stage : Stages.values()) {
        if (!stages.contains(stage))
          creator.setStageToSkip(stage);
      }
    }

    if (commandLine.hasOption(ARG_SKIP)) {
      String[] values = commandLine.getOptionValues(ARG_SKIP);
      for (String value : values)
        creator.setStageToSkip(Stages.valueOf(value));
    }
  }
}
