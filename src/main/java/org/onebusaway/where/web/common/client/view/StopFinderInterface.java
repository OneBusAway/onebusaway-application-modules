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
package org.onebusaway.where.web.common.client.view;

import org.onebusaway.common.web.common.client.context.ContextListener;
import org.onebusaway.where.web.common.client.model.StopsBean;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface StopFinderInterface extends ContextListener {

  public AsyncCallback<StopsBean> getStopsHandler();
  
  public void setSearchText(EWhereStopFinderSearchType type, String value);

  public void queryAddress(String address);
  
  public void queryLocation(LatLng location, int accuracy);
  
  public void queryArea(LatLngBounds bounds);
  
  public void queryRoute(String route);

  public void queryRoute(String route, String blockId);

  public void queryStopSequence(String route, int sequenceId);

  public void queryStop(String stopId);
  
  public void showStopIdentificationInfo();
}
