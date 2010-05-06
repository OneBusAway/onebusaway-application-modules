package org.onebusaway.cli;

import java.lang.reflect.Method;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

public class DaemonizerMain {

  private static final String ARG_MAIN_CLASS = "mainClass";

  private static final String ARG_ARGS = "args";

  public static void main(String[] args) throws Exception {

    Options options = new Options();
    Daemonizer.buildOptions(options);

    options.addOption(ARG_MAIN_CLASS, true, "main class");
    options.addOption(ARG_ARGS, true, "arguments");

    GnuParser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);

    Daemonizer.handleDaemonization(cli);

    String mainClassValue = cli.getOptionValue(ARG_MAIN_CLASS);
    if (mainClassValue == null) {
      usage();
      System.exit(-1);
    }

    String argValues = cli.getOptionValue(ARG_ARGS);
    String[] newArgArray = argValues.split("\\s+");

    Class<?> mainClass = Class.forName(mainClassValue);
    Method method = mainClass.getMethod("main", new Class<?>[] {String[].class});

    method.invoke(null, (Object) newArgArray);
  }

  private static void usage() {
    System.out.println("usage: -mainClass path.to.your.MainClass");
  }
}
