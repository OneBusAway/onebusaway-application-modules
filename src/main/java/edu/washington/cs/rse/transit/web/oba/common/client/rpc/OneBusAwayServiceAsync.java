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

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

import edu.washington.cs.rse.transit.web.oba.common.client.model.NameTreeBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.RouteBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternPathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternTimeBlocksBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithArrivalsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithRoutesBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopsBean;

public interface OneBusAwayServiceAsync extends RemoteService {

    public static OneBusAwayServiceAsync SERVICE = GWT.create(OneBusAwayService.class);

    public void getStopsByLocationAndAccuracy(double lat, double lon, int accuracy, AsyncCallback<StopsBean> callback);

    public void getStopsByBounds(double lat1, double lon1, double lat2, double lon2, AsyncCallback<StopsBean> callback);

    public void getStop(int stopId, AsyncCallback<StopWithRoutesBean> callback);

    public void getArrivalsByStopId(int stopId, AsyncCallback<StopWithArrivalsBean> callback);

    public void getStopByRoute(int routeNumber, List<Integer> selection, AsyncCallback<NameTreeBean> callback);

    public void getServicePatternTimeBlocksByRoute(int routeNumber, AsyncCallback<ServicePatternTimeBlocksBean> callback);

    public void getServicePatternPath(int servicePatternId, AsyncCallback<ServicePatternPathBean> callback);

    public void getTransLinkPath(int transLinkId, AsyncCallback<PathBean> callback);

    public void getActiveRoutes(AsyncCallback<List<RouteBean>> callback);

    public void getActiveServicePatternsByRoute(int routeNumber, AsyncCallback<List<ServicePatternBean>> callback);

    public void getActiveStopsByServicePattern(int id, AsyncCallback<StopsBean> callback);
}
