/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.api;

import org.onebusaway.users.services.CurrentUserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * Make the user service available to subclasses, and provide convenience
 * check to test authorization. 
 *
 */
public class AuthenticatedResource {

  private static Logger _log = LoggerFactory.getLogger(AuthenticatedResource.class);
  protected CurrentUserService _currentUserService;
  
  @Autowired
  public void setCurrentUserService(CurrentUserService userDataService) {
    _currentUserService = userDataService;
  }

  protected boolean isAuthorized() {
    boolean isAuthorized = _currentUserService.isCurrentUserAdmin();
    if (!isAuthorized) {
      _log.info("request not authorized");
    }
    return isAuthorized;
  }

}
