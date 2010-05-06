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
package edu.washington.cs.rse.transit.web.oba.common.client.rpc;

import edu.washington.cs.rse.transit.web.oba.common.client.model.NameTreeBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlocksBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlockPathsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithArrivalsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithRoutesBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopsBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;

public interface OneBusAwayServiceAsync extends RemoteService {

  public static OneBusAwayServiceAsync SERVICE = GWT.create(OneBusAwayService.class);

  public void getStopsByLocationAndAccuracy(double lat, double lon,
      int accuracy, AsyncCallback<StopsBean> callback);

  public void getStopsByBounds(double lat1, double lon1, double lat2,
      double lon2, AsyncCallback<StopsBean> callback);

  public void getStop(String stopId, AsyncCallback<StopWithRoutesBean> callback);

  public void getArrivalsByStopId(String stopId,
      AsyncCallback<StopWithArrivalsBean> callback);

  public void getStopByRoute(String routeNumber, List<Integer> selection,
      AsyncCallback<NameTreeBean> callback);

  public void getServicePatternBlocksByRoute(String routeNumber,
      AsyncCallback<ServicePatternBlocksBean> callback);

  public void getServicePatternPath(String route, String servicePatternId,
      AsyncCallback<ServicePatternBlockPathsBean> callback);

  public void getActiveStopsByServicePattern(String route,
      String servicePatternId, AsyncCallback<StopsBean> callback);
}
