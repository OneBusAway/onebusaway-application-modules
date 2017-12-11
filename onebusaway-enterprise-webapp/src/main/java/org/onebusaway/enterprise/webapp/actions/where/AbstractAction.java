/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.enterprise.webapp.actions.where;

import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.presentation.impl.NextActionSupport;
import org.onebusaway.presentation.services.ServiceAreaService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;

import org.springframework.beans.factory.annotation.Autowired;



public abstract class AbstractAction extends NextActionSupport {

  private static final long serialVersionUID = 1L;

  protected TransitDataService _transitDataService;

  protected CurrentUserService _currentUserService;

  private ServiceAreaService _serviceAreaService;

  public void setSession(Map<String, Object> session) {
    _session = session;
  }

  @Autowired
  public void setServiceAreaService(ServiceAreaService serviceAreaService) {
    _serviceAreaService = serviceAreaService;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }


  public boolean isAnonymousUser() {
    return _currentUserService.isCurrentUserAnonymous();
  }

  /****
   * Protected Methods
   ****/

  protected CoordinateBounds getServiceArea() {
    return _serviceAreaService.getServiceArea();
  }

  protected UserBean getCurrentUser() {
    UserBean user = _currentUserService.getCurrentUser();
    if (user == null)
      user = _currentUserService.getAnonymousUser();
    return user;
  }
}
