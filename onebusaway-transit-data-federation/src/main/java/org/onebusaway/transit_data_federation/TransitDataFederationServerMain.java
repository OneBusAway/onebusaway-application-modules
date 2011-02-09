package org.onebusaway.transit_data_federation;

import org.onebusaway.container.ContainerLibrary;

import java.util.ArrayList;
import java.util.List;

public class TransitDataFederationServerMain {
  public static void main(String[] args) {
    List<String> sources = new ArrayList<String>();
    for (String arg : args)
      sources.add("file:" + arg);
    sources.add("classpath:org/onebusaway/transit_data_federation/application-context.xml");
    ContainerLibrary.createContext(sources);
  }
}
