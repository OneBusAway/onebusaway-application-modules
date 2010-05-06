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
package edu.washington.cs.rse.transit.web.actions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.web.oba.common.client.model.RouteBean;

public class RoutesAction extends ActionSupport {

    private static final long serialVersionUID = 1L;

    private MetroKCDAO _dao;

    private List<RouteBean> _routes = new ArrayList<RouteBean>();

    @Autowired
    public void setMetroKCDAO(MetroKCDAO dao) {
        _dao = dao;
    }

    public List<RouteBean> getRoutes() {
        return _routes;
    }

    public String execute() throws Exception {

        List<Route> routes = _dao.getActiveRoutes();

        for (Route route : routes) {
            RouteBean rb = new RouteBean();
            rb.setNumber(route.getNumber());
            _routes.add(rb);
        }

        return SUCCESS;
    }
}
