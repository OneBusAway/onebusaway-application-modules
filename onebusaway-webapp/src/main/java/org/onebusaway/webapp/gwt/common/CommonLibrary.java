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
package org.onebusaway.webapp.gwt.common;

import org.onebusaway.webapp.gwt.common.resources.CommonResources;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class CommonLibrary implements EntryPoint {

  public void onModuleLoad() {
    StyleInjector.inject(CommonResources.INSTANCE.getCss().getText());
  }

  public static void registerService(String servicePath, RemoteService service) {
    String root = getServiceRoot();
    ServiceDefTarget target = (ServiceDefTarget) service;
    String path = root + servicePath;
    target.setServiceEntryPoint(path);
    System.out.println("service entry point: " + path);
  }

  private static String getServiceRoot() {

    try {
      Dictionary dictionary = Dictionary.getDictionary("OneBusAwayConfig");
      String potentialServiceRoot = dictionary.get("ServiceRoot");
      if (potentialServiceRoot != null)
        return potentialServiceRoot;
    } catch (Exception ex) {

    }

    if (Location.getProtocol().equals("file:"))
      return "http://localhost:8080/onebusaway-webapp";

    if (Location.getHost().equals("localhost:8888")) {
      String path = Location.getPath();
      int index = path.lastIndexOf('/');
      path = path.substring(0, index);
      return "http://localhost:8888" + path;
    }

    String path = Location.getPath();
    if (path.startsWith("/onebusaway-webapp"))
      return Location.getProtocol() + "//" + Location.getHost()
          + "/onebusaway-webapp";

    return Location.getProtocol() + "//" + Location.getHost();
  }
}
