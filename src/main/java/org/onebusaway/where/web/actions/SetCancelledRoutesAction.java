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
package org.onebusaway.where.web.actions;

import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.where.services.StatusService;

import com.opensymphony.xwork2.ActionSupport;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

public class SetCancelledRoutesAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private StatusService _statusService;

  private String _routes;

  public void setRoutes(String routes) {
    _routes = routes;
  }

  @Override
  public String execute() throws ServiceException {

    String routes = _routes.trim();
    routes = routes.replaceAll(",", " ");
    Set<String> routeShortNames = new HashSet<String>();
    for (String routeToken : routes.split("\\s+"))
      routeShortNames.add(routeToken);

    _statusService.setCancelledRoutes(routeShortNames);

    return SUCCESS;
  }
}
