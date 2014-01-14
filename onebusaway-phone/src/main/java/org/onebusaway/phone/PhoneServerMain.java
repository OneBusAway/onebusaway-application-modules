/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
