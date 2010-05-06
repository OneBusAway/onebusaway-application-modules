package org.onebusaway.phone;

import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.container.stop.StopButtonService;

public class PhoneServerMain {
  public static void main(String[] args) {
    ContainerLibrary.createContext("classpath:org/onebusaway/phone/application-context.xml","classpath:data-sources.xml");
    StopButtonService button = new StopButtonService();
    button.start();
  }
}
