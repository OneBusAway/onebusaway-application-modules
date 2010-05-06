package org.onebusaway.kcmetro_tcip;

import org.onebusaway.container.stop.StopButtonService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;

import java.io.File;
import java.util.Arrays;

public class TcipServerMain {

  private static final String ARG_DAEMONIZE = "daemonize";
  private static final String ARG_PID_FILE = "pidFile";
  private static final String ARG_JVM_ARGS = "jvmArgs";
  private static final String ARG_ERROR_FILE = "errorFile";
  private static final String ARG_OUTPUT_FILE = "outputFile";

  public static void main(String[] args) throws Exception {

    Options options = buildOptions();

    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);

    boolean isDaemon = handleDaemonization(cli);

    args = cli.getArgs();

    if (args.length == 0) {
      printUsage();
      System.exit(-1);
    }

    TcipServerEntryPoint.run(args);

    if (!isDaemon) {
      StopButtonService service = new StopButtonService();
      service.start();
    }
  }

  private static void printUsage() {
    System.err.println("usage: [-args ...] path/to/data-sources.xml");
    System.err.println();
    System.err.println("args:");
    System.err.println(" -daemonize            run as daemon");
    System.err.println(" -pidFile path         write the pid of the daemon jvm instance process to the specified path");
    System.err.println(" -jvmArgs \"...\"        additional jvm arguments to be passed to the daemon jvm instance");
    System.err.println(" -errorFile path       daemon stderr written to the specified path (\"&1\" for stdout redirect)");
    System.err.println(" -outputFile path      daemon stdout written to the specified path (\"&2\" for stderr redirect)");
  }

  private static Options buildOptions() {
    Options options = new Options();
    options.addOption(ARG_JVM_ARGS, true, "custom jvm args for the daemon");
    options.addOption(ARG_OUTPUT_FILE, true,
        "stdout output (&2 to redirect to stderr)");
    options.addOption(ARG_ERROR_FILE, true,
        "stderr output (&1 to redirect to stdout)");
    options.addOption(ARG_PID_FILE, true, "pid file");
    options.addOption(ARG_DAEMONIZE, false, "run as a daemon");
    return options;
  }

  private static boolean handleDaemonization(CommandLine cli) throws Exception {
    if (cli.hasOption(ARG_DAEMONIZE)) {

      Daemonizer daemonizer = new Daemonizer();

      if (cli.hasOption(ARG_OUTPUT_FILE))
        daemonizer.setOutputFile(new File(cli.getOptionValue(ARG_OUTPUT_FILE)));

      if (cli.hasOption(ARG_ERROR_FILE))
        daemonizer.setErrorFile(new File(cli.getOptionValue(ARG_ERROR_FILE)));

      if (cli.hasOption(ARG_PID_FILE))
        daemonizer.setPidFile(new File(cli.getOptionValue(ARG_PID_FILE)));

      if (cli.hasOption(ARG_JVM_ARGS)) {
        String[] jvmArgs = cli.getOptionValue(ARG_JVM_ARGS).split(" ");
        daemonizer.setJvmArgs(Arrays.asList(jvmArgs));
      }

      daemonizer.daemonize();
      return true;
    }

    return false;
  }
}
