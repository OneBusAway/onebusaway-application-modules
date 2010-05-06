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
package org.onebusaway.where.web.common.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

import org.onebusaway.where.web.common.client.model.NameTreeBean;
import org.onebusaway.where.web.common.client.model.StopBean;
import org.onebusaway.where.web.common.client.model.StopScheduleBean;
import org.onebusaway.where.web.common.client.model.StopSequenceBlockBean;
import org.onebusaway.where.web.common.client.model.StopWithArrivalsBean;
import org.onebusaway.where.web.common.client.model.StopsBean;

import java.util.Date;
import java.util.List;

public interface WhereService extends RemoteService {

  public StopsBean getStopsByLocationAndAccuracy(double lat, double lon,
      int accuracy) throws ServiceException;

  public StopsBean getStopsByBounds(double lat1, double lon1, double lat2,
      double lon2) throws ServiceException;

  public StopBean getStop(String stopId) throws ServiceException;

  public StopWithArrivalsBean getArrivalsByStopId(String stopId)
      throws ServiceException;

  public NameTreeBean getStopByRoute(String route, List<Integer> selection)
      throws ServiceException;

  public List<StopSequenceBlockBean> getStopSequenceBlocksByRoute(String route)
      throws ServiceException;

  public StopScheduleBean getScheduleForStop(String stopId, Date date)
      throws ServiceException;
}
