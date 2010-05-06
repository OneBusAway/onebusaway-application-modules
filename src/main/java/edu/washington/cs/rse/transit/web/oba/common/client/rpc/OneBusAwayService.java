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

public interface OneBusAwayService extends RemoteService {

    public StopsBean getStopsByLocationAndAccuracy(double lat, double lon, int accuracy) throws ServiceException;

    public StopsBean getStopsByBounds(double lat1, double lon1, double lat2, double lon2) throws ServiceException;

    public StopWithRoutesBean getStop(int stopId) throws ServiceException;

    public StopWithArrivalsBean getArrivalsByStopId(int stopId) throws ServiceException;

    public NameTreeBean getStopByRoute(int routeNumber, List<Integer> selection) throws ServiceException;

    public ServicePatternTimeBlocksBean getServicePatternTimeBlocksByRoute(int routeNumber) throws ServiceException;

    public ServicePatternPathBean getServicePatternPath(int servicePatternId) throws ServiceException;

    public PathBean getTransLinkPath(int transLinkId) throws ServiceException;

    public List<RouteBean> getActiveRoutes() throws ServiceException;

    public List<ServicePatternBean> getActiveServicePatternsByRoute(int routeNumber) throws ServiceException;

    public StopsBean getActiveStopsByServicePattern(int id) throws ServiceException;
}
