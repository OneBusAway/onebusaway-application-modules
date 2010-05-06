/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.webapp.gwt.nearby_routes_mobile;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.user.client.ui.RootPanel;

public class NearbyRoutesIPhoneApplication implements EntryPoint {

  public void onModuleLoad() {
    addJavascriptCallback();
    Dictionary d = Dictionary.getDictionary("nearbyRoutesConfig");
    getNearbyRoutes(d.get("elementId"), d.get("stopId"));
  }

  public static void getNearbyRoutes(String elementId, String stopId) {
    RootPanel panel = RootPanel.get(elementId);
    //service.getNearbyRoutes(stopId, 5280 / 4, new NearbyRouteHandler(panel));
  }

  private native void addJavascriptCallback() /*-{
       $wnd.getNearbyRoutes = function (elementId,stopId) {
         @org.onebusaway.where.web.iphone.client.NearbyRoutesIPhoneApplication::getNearbyRoutes(Ljava/lang/String;Ljava/lang/String;)(elementId,stopId)
      };
    }-*/;
}
