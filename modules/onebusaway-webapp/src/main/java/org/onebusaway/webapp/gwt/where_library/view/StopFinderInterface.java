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
package org.onebusaway.webapp.gwt.where_library.view;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.common.context.ContextListener;

import com.google.gwt.maps.client.geom.LatLng;

import java.util.List;

public interface StopFinderInterface extends ContextListener {

  public void setCenter(LatLng center, int accuracy);
  
  public void setShowStopsInCurrentView(boolean showStopsInCurrentView);

  public void showStops(List<StopBean> stops);

  public void setSearchText(String value);

  public void queryAddress(String address);

  public void queryLocation(LatLng location, int accuracy);

  public void queryRoutes(String routeQuery);

  public void queryRoute(String routeId);

  public void queryStop(String stopId);

  public void showStopIdentificationInfo();
}
