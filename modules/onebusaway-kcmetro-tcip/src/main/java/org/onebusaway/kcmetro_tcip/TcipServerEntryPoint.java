package org.onebusaway.kcmetro_tcip;

import org.onebusaway.container.ContainerLibrary;

import java.util.ArrayList;
import java.util.List;

public class TcipServerEntryPoint {
  
  public static void run(String[] args) {

    List<String> resources = new ArrayList<String>();

    resources.add("classpath:org/onebusaway/kcmetro_tcip/application-context.xml");

    for (String arg : args) {
      if (!(arg.startsWith("file:") || arg.startsWith("classpath:")))
        arg = "file:" + arg;
      resources.add(arg);
    }

    ContainerLibrary.createContext(resources);
  }
}
