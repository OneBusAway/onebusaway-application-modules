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
package edu.washington.cs.rse.transit.web.oba.common.server;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.washington.cs.rse.transit.web.oba.common.client.model.NameTreeBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.RouteBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternPathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternTimeBlocksBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithArrivalsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithRoutesBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.OneBusAwayService;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.ServiceException;

public class OneBusAwayServiceServletImpl extends RemoteServiceServlet implements OneBusAwayService {

    private static final long serialVersionUID = 1L;

    private OneBusAwayService _service;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        context.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Autowired
    public void setOneBusAwayService(OneBusAwayService service) {
        _service = service;
    }

    public StopsBean getStopsByLocationAndAccuracy(double lat, double lon, int accuracy) throws ServiceException {
        return _service.getStopsByLocationAndAccuracy(lat, lon, accuracy);
    }

    public StopsBean getStopsByBounds(double lat1, double lon1, double lat2, double lon2) throws ServiceException {
        return _service.getStopsByBounds(lat1, lon1, lat2, lon2);
    }

    public StopWithRoutesBean getStop(int stopId) throws ServiceException {
        return _service.getStop(stopId);
    }

    public StopWithArrivalsBean getArrivalsByStopId(int stopId) throws ServiceException {
        return _service.getArrivalsByStopId(stopId);
    }

    public NameTreeBean getStopByRoute(int routeNumber, List<Integer> selection) throws ServiceException {
        return _service.getStopByRoute(routeNumber, selection);
    }

    public ServicePatternTimeBlocksBean getServicePatternTimeBlocksByRoute(int routeNumber) throws ServiceException {
        return _service.getServicePatternTimeBlocksByRoute(routeNumber);
    }

    public ServicePatternPathBean getServicePatternPath(int servicePatternId) throws ServiceException {
        return _service.getServicePatternPath(servicePatternId);
    }

    public PathBean getTransLinkPath(int transLinkId) throws ServiceException {
        return _service.getTransLinkPath(transLinkId);
    }

    public List<RouteBean> getActiveRoutes() throws ServiceException {
        return _service.getActiveRoutes();
    }

    public List<ServicePatternBean> getActiveServicePatternsByRoute(int routeNumber) throws ServiceException {
        return _service.getActiveServicePatternsByRoute(routeNumber);
    }

    public StopsBean getActiveStopsByServicePattern(int id) throws ServiceException {
        return _service.getActiveStopsByServicePattern(id);
    }
}
