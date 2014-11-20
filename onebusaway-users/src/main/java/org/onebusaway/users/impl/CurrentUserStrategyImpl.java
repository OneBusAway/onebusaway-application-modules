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

import org.onebusaway.users.impl.authentication.DefaultUserAuthenticationToken;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUserStrategyImpl implements CurrentUserStrategy {

  private UserService _userService;

  private StandardAuthoritiesService _authoritiesService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @Autowired
  public void setAuthoritiesService(
      StandardAuthoritiesService authoritiesService) {
    _authoritiesService = authoritiesService;
  }

  @Override
  public User getCurrentUser(boolean createUserIfAppropriate) {
    UserIndex userIndex = getCurrentUserIndex(createUserIfAppropriate);
    if (userIndex == null)
      return null;
    return userIndex.getUser();
  }

  public UserIndex getCurrentUserIndex(boolean createUserIfAppropriate) {

    IndexedUserDetails details = getCurrentUserDetails(createUserIfAppropriate);

    if (details == null)
      return null;

    return _userService.getUserIndexForId(details.getUserIndexKey());
  }

  @Override
  public IndexedUserDetails getCurrentUserDetails(
      boolean createUserIfAppropriate) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null)
      return null;

    return getUserDetailsForAuthentication(authentication);
  }

  @Override
  public void setCurrentUser(UserIndex userIndex) {

    IndexedUserDetails userDetails = new IndexedUserDetailsImpl(
        _authoritiesService, userIndex);

    DefaultUserAuthenticationToken token = new DefaultUserAuthenticationToken(
        userDetails);
    SecurityContextHolder.getContext().setAuthentication(token);
  }

  @Override
  public void clearCurrentUser() {
    // Log out the current user
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  protected IndexedUserDetails getUserDetailsForAuthentication(
      Authentication authentication) {
    // The principal really shouldn't be a UserDetails object, yet that is where
    // the RememberMe authentication service puts it
    Object principal = authentication.getPrincipal();
    if (principal instanceof IndexedUserDetails)
      return (IndexedUserDetails) principal;

    Object details = authentication.getDetails();
    if (details instanceof IndexedUserDetails)
      return (IndexedUserDetails) details;

    return null;
  }
}
