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
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlockPathsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlocksBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithArrivalsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithRoutesBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopsBean;

import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;

public interface OneBusAwayService extends RemoteService {

  public StopsBean getStopsByLocationAndAccuracy(double lat, double lon,
      int accuracy) throws ServiceException;

  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2,
      double lon2) throws ServiceException;

  public StopWithRoutesBean getStop(String stopId) throws ServiceException;

  public StopWithArrivalsBean getArrivalsByStopId(String stopId)
      throws ServiceException;

  public NameTreeBean getStopByRoute(String route, List<Integer> selection)
      throws ServiceException;

  public ServicePatternBlocksBean getServicePatternBlocksByRoute(String route)
      throws ServiceException;

  public ServicePatternBlockPathsBean getServicePatternPath(String route, String servicePatternId)
      throws ServiceException;

  public StopsBean getActiveStopsByServicePattern(String route,
      String servicePatternId) throws ServiceException;

}
