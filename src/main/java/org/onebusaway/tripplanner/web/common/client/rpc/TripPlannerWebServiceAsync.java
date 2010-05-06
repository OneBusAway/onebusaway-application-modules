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
package org.onebusaway.tripplanner.web.common.client.rpc;

import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripPlannerConstraintsBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;

public interface TripPlannerWebServiceAsync extends RemoteService {

  public static final String SERVICE_PATH = "/services/tripplanner";

  public static TripPlannerWebServiceAsync SERVICE = GWT.create(TripPlannerWebService.class);

  public void getTripsBetween(double latFrom, double lonFrom, double latTo, double lonTo,
      TripPlannerConstraintsBean constraints, AsyncCallback<List<TripBean>> callbkack);
}
