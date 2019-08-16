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
package org.onebusaway.users.services;

import org.onebusaway.users.model.UserRole;

import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface StandardAuthoritiesService {

  public final static String ANONYMOUS = "ROLE_ANONYMOUS";
  
  public final static String USER = "ROLE_USER";

  public final static String ADMINISTRATOR = "ROLE_ADMINISTRATOR";
  
  public final static String OPERATOR = "ROLE_OPERATOR";
  
  public final static String SUPPORT = "ROLE_SUPPORT";
  
  public final static String REPORTING = "ROLE_REPORTING";
  

  /**
   * This should be the only place where the standard authorities for the app
   * are defined
   */
  public final static List<String> STANDARD_AUTHORITIES = Collections.unmodifiableList(Arrays.asList(
      ADMINISTRATOR, OPERATOR, SUPPORT, REPORTING, ANONYMOUS, USER));

  public final static List<String> MANAGED_AUTHORITIES = Collections.unmodifiableList(Arrays.asList(
          ADMINISTRATOR, OPERATOR, SUPPORT, REPORTING));


  /**
   * Get the GrantedAuthority used by Spring Security for the role by name
   * 
   * @param name Role name like {@link #USER}
   * @return Spring Security authority
   */
  public GrantedAuthority getNameBasedAuthority(String name);

  public UserRole getUserRoleForName(String name);

  public UserRole getAnonymousRole();

  public UserRole getUserRole();

  public UserRole getAdministratorRole();
  
  public UserRole getReportingRole();
}
