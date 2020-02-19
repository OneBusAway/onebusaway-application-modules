/**
 * Copyright (C) 2013 Kurt Raschke
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.enterprise.webapp.actions.m.routes;

import java.util.List;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.presentation.services.routes.RouteListService;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action for route index page
 *
 */
public class IndexAction extends OneBusAwayEnterpriseActionSupport {

    private static final long serialVersionUID = 1L;
    @Autowired
    private RouteListService _routeListService;

    private String _agencyFilter = null;

    public void setAgencyFilter(String filter) {
        _agencyFilter = filter;
    }

    public boolean getShowAgencyNames() {
        return _routeListService.getShowAgencyNames();
    }

    public boolean getUseAgencyId() {
        return _routeListService.getUseAgencyId();
    }

    public List<RouteBean> getRoutes() {
        if (_agencyFilter != null)
            return _routeListService.getFilteredRoutes(_agencyFilter);
        return _routeListService.getRoutes();

    }
}
