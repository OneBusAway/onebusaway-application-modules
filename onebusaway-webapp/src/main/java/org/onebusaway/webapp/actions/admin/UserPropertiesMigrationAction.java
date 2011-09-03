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
package org.onebusaway.webapp.actions.admin;

import org.onebusaway.users.services.UserPropertiesMigrationStatus;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class UserPropertiesMigrationAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private UserService _userService;

  private UserPropertiesMigrationStatus _status;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public UserPropertiesMigrationStatus getStatus() {
    return _status;
  }

  @Override
  public String execute() {
    _status = _userService.getUserPropertiesMigrationStatus();
    return SUCCESS;
  }
}
