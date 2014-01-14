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
package org.onebusaway.users.impl;

import javax.annotation.PostConstruct;

import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateUserAction {

  private UserService _userService;

  private String username;

  private String password;

  private boolean admin;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  @PostConstruct
  public void execute() {

    UserIndex userIndex = _userService.getOrCreateUserForUsernameAndPassword(
        username, password);

    if (userIndex == null)
      throw new IllegalStateException("error creating user");

    if (admin) {
      User user = userIndex.getUser();
      _userService.enableAdminRoleForUser(user, false);
    }
  }
}
