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

import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.springframework.security.core.GrantedAuthority;

public class MockStandardAuthoritiesServiceImpl implements
    StandardAuthoritiesService {

  private UserRole _admin = new UserRole(ADMINISTRATOR);

  private UserRole _anonymous = new UserRole(ANONYMOUS);

  private UserRole _user = new UserRole(USER);
  
  private UserRole _reporting = new UserRole(REPORTING);

  @Override
  public UserRole getAdministratorRole() {
    return _admin;
  }

  @Override
  public UserRole getAnonymousRole() {
    return _anonymous;
  }

  @Override
  public UserRole getUserRole() {
    return _user;
  }
  
  @Override
  public UserRole getReportingRole() {
    return _reporting;
  }

  @Override
  public GrantedAuthority getNameBasedAuthority(String name) {
    return null;
  }

  @Override
  public UserRole getUserRoleForName(String name) {
    return null;
  }

}
