/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results({@Result(type = "redirectAction", name = "redirect", params = {
    "actionName", "stale-users"})})
public class StaleUsersAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private UserService _userService;

  private long _numberOfStaleUsers = 0;

  private boolean _deletingStaleUsers = false;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public long getNumberOfStaleUsers() {
    return _numberOfStaleUsers;
  }

  public boolean isDeletingStaleUsers() {
    return _deletingStaleUsers;
  }

  @Override
  public String execute() {
    _numberOfStaleUsers = _userService.getNumberOfStaleUsers();
    _deletingStaleUsers = _userService.isDeletingStaleUsers();
    return SUCCESS;
  }

  public String start() {
    _userService.deleteStaleUsers();
    return "redirect";
  }

  public String stop() {
    _userService.cancelDeleteStaleUsers();
    return "redirect";
  }

}
