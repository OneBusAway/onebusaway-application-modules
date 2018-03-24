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

import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.IndexedUserDetailsService;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserService;
import org.onebusaway.users.services.internal.UserLastAccessTimeService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserDetailsServiceImpl implements UserDetailsService,
    IndexedUserDetailsService {

  private UserService _userService;

  private StandardAuthoritiesService _authoritiesService;

  private UserLastAccessTimeService _userLastAccessTimeService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @Autowired
  public void setAuthoritiesService(
      StandardAuthoritiesService authoritiesService) {
    _authoritiesService = authoritiesService;
  }
  
  @Autowired
  public void setUserLastAccessTimeService(UserLastAccessTimeService userLastAccessTimeService) {
    _userLastAccessTimeService = userLastAccessTimeService;
  }

  /****
   * {@link UserDetailsService} Interface
   ****/

  @Transactional
  @Override
  public UserDetails loadUserByUsername(String username)
      throws UsernameNotFoundException, DataAccessException {

    int index = username.indexOf('_');
    if (index == -1)
      throw new UsernameNotFoundException(
          "username did not take the form type_value: " + username);

    String type = username.substring(0, index);
    String value = username.substring(index + 1);

    UserIndexKey key = new UserIndexKey(type, value);
    return getUserForIndexKey(key);
  }

  /****
   * {@link IndexedUserDetailsService} Interface
   ****/

  @Transactional
  @Override
  public IndexedUserDetails getUserForIndexKey(UserIndexKey key)
      throws UsernameNotFoundException, DataAccessException {

    UserIndex userIndex = _userService.getUserIndexForId(key);

    if (userIndex == null)
      throw new UsernameNotFoundException(key.toString());
    
    setLastAccessTimeForUser(userIndex);
    
    return new IndexedUserDetailsImpl(_authoritiesService, userIndex);
  }

  @Override
  public IndexedUserDetails getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials, boolean isAnonymous) throws DataAccessException {

    UserIndex userIndex = _userService.getOrCreateUserForIndexKey(key,
        credentials, isAnonymous);
    
    setLastAccessTimeForUser(userIndex);
    
    return new IndexedUserDetailsImpl(_authoritiesService, userIndex);
  }

  private void setLastAccessTimeForUser(UserIndex userIndex) {
    User user = userIndex.getUser();
    _userLastAccessTimeService.handleAccessForUser(user.getId(), SystemTime.currentTimeMillis());
  }

  @Override
  public void resetUserForIndexKey(UserIndexKey key) {
    UserIndex index = _userService.getUserIndexForId(key);
    if( index != null)
      _userService.resetUser(index.getUser());
  }
}
