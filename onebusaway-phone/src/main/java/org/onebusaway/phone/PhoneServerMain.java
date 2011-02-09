package org.onebusaway.phone;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.onebusaway.cli.Daemonizer;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.container.stop.StopButtonService;

public class PhoneServerMain {

  private static final String ARG_RESOURCES = "resources";

  public static void main(String[] args) throws Exception {

    Options options = new Options();
    Daemonizer.buildOptions(options);
    options.addOption(ARG_RESOURCES, true, "resources");

    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);

    boolean isDaemon = Daemonizer.handleDaemonization(cli);

    List<String> resources = getResources(cli);
    ContainerLibrary.createContext(resources);

    if (isDaemon) {
      // We need a running non-daemon thread to avoid having the program
      // immediately exit
      new Thread(new Runnable() {
        @Override
        public synchronized void run() {
          try {
            wait();
          } catch (InterruptedException e) {

          }
        }
      }).start();
    } else {
      StopButtonService button = new StopButtonService();
      button.start();
    }
  }

  private static List<String> getResources(CommandLine cli) {
    List<String> resources = new ArrayList<String>();
    resources.add("classpath:org/onebusaway/phone/application-context.xml");

    if (cli.hasOption(ARG_RESOURCES)) {
      String[] tokens = cli.getOptionValue(ARG_RESOURCES).split(",");
      for (String token : tokens)
        resources.add(token);
    } else {
      resources.add("classpath:data-sources.xml");
    }
    return resources;
  }
}
