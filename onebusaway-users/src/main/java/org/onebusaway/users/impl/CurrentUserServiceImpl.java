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

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserServiceImpl implements CurrentUserService {

  private enum Mode {
    LOGIN, REGISTRATION, ADD_ACCOUNT
  };

  private UserService _userService;

  private UserPropertiesService _userPropertiesService;

  private StandardAuthoritiesService _authoritiesService;

  private CurrentUserStrategy _currentUserStrategy;

  @Autowired
  public void setUserService(UserService service) {
    _userService = service;
  }

  @Autowired
  public void setUserPropertiesService(
      UserPropertiesService userPropertiesService) {
    _userPropertiesService = userPropertiesService;
  }

  @Autowired
  public void setAuthoritiesService(
      StandardAuthoritiesService authoritiesService) {
    _authoritiesService = authoritiesService;
  }

  @Autowired
  public void setCurrentUserStrategy(CurrentUserStrategy currentUserStrategy) {
    _currentUserStrategy = currentUserStrategy;
  }

  /****
   * {@link CurrentUserService} Interface
   ****/

  @Override
  public boolean isCurrentUserAnonymous() {
    IndexedUserDetails details = _currentUserStrategy.getCurrentUserDetails(false);
    if (details == null)
      return true;
    return details.isAnonymous();
  }

  @Override
  public boolean isCurrentUserAdmin() {
    IndexedUserDetails details = _currentUserStrategy.getCurrentUserDetails(false);
    if (details == null)
      return false;
    return details.isAdmin();
  }

  @Override
  public boolean isCurrentUserReporting() {
    IndexedUserDetails details = _currentUserStrategy.getCurrentUserDetails(false);
    if (details == null)
      return false;
    return details.isReporting();
  }
  
  @Override
  public IndexedUserDetails getCurrentUserDetails() {
    return _currentUserStrategy.getCurrentUserDetails(false);
  }

  @Override
  public UserBean getCurrentUser() {
    return getCurrentUser(false);
  }

  @Override
  public UserBean getCurrentUser(boolean createUserIfAppropriate) {
    UserIndex userIndex = _currentUserStrategy.getCurrentUserIndex(createUserIfAppropriate);
    if (userIndex == null)
      return null;
    return _userService.getUserAsBean(userIndex.getUser());
  }

  @Override
  public UserIndex getCurrentUserAsUserIndex() {
    return _currentUserStrategy.getCurrentUserIndex(false);
  }

  @Override
  public UserBean getAnonymousUser() {
    return _userService.getAnonymousUser();
  }

  @Override
  public IndexedUserDetails handleUserAction(String type, String id,
      String credentials, boolean isAnonymous, String mode) {
    Mode m = getModeForRequest(mode);
    switch (m) {
      case LOGIN:
        return handleLogin(type, id, credentials, isAnonymous, true);
      case REGISTRATION:
        return handleRegistration(type, id, credentials, isAnonymous);
      case ADD_ACCOUNT:
        return handleAddAccount(type, id, credentials, isAnonymous);
    }
    throw new IllegalStateException("unknown mode: " + mode + " " + m);
  }

  @Override
  public IndexedUserDetails handleLogin(String type, String id,
      String credentials, boolean isAnonymous, boolean registerIfNewUser) {

    UserIndexKey key = new UserIndexKey(type, id);
    UserIndex index = _userService.getUserIndexForId(key);
    boolean exists = index != null;

    // New user?
    if (!exists) {

      if (!registerIfNewUser)
        return null;

      index = _userService.getOrCreateUserForIndexKey(key, credentials, false);
      User newUser = index.getUser();

      User oldUser = _currentUserStrategy.getCurrentUser(false);
      if (oldUser != null && _userService.isAnonymous(oldUser))
        _userService.mergeUsers(oldUser, newUser);
    }

    return new IndexedUserDetailsImpl(_authoritiesService, index);
  }

  @Override
  public IndexedUserDetails handleRegistration(String type, String id,
      String credentials, boolean isAnonymous) {

    UserIndexKey key = new UserIndexKey(type, id);
    UserIndex index = _userService.getOrCreateUserForIndexKey(key, credentials,
        isAnonymous);

    User oldUser = _currentUserStrategy.getCurrentUser(false);
    if (oldUser != null && _userService.isAnonymous(oldUser))
      _userService.mergeUsers(oldUser, index.getUser());

    return new IndexedUserDetailsImpl(_authoritiesService, index);
  }

  @Override
  public IndexedUserDetails handleAddAccount(String type, String id,
      String credentials, boolean isAnonymous) {

    User currentUser = _currentUserStrategy.getCurrentUser(false);

    UserIndexKey key = new UserIndexKey(type, id);
    UserIndex index = _userService.getUserIndexForId(key);
    boolean exists = index != null;

    // New user?
    if (exists) {
      if (currentUser != null) {
        User existingUser = index.getUser();
        _userService.mergeUsers(existingUser, currentUser);
      }
    } else {
      if (currentUser != null)
        index = _userService.addUserIndexToUser(currentUser, key, credentials);
      else
        index = _userService.getOrCreateUserForIndexKey(key, credentials,
            isAnonymous);
    }

    return new IndexedUserDetailsImpl(_authoritiesService, index);
  }

  @Override
  public void setDefaultLocation(String locationName, double lat, double lon) {

    User user = _currentUserStrategy.getCurrentUser(true);
    if (user == null)
      return;
    _userPropertiesService.setDefaultLocation(user, locationName, lat, lon);
  }

  @Override
  public void clearDefaultLocation() {
    User user = _currentUserStrategy.getCurrentUser(false);
    if (user == null)
      return;
    _userPropertiesService.clearDefaultLocation(user);
  }

  @Override
  public int addStopBookmark(String name, List<String> stopIds,
      RouteFilter filter) {
    User user = _currentUserStrategy.getCurrentUser(true);
    if (user == null)
      return -1;
    return _userPropertiesService.addStopBookmark(user, name, stopIds, filter);
  }

  @Override
  public void updateStopBookmark(int id, String name, List<String> stopIds,
      RouteFilter routeFilter) {

    User user = _currentUserStrategy.getCurrentUser(false);
    if (user == null)
      return;
    _userPropertiesService.updateStopBookmark(user, id, name, stopIds,
        routeFilter);
  }

  @Override
  public void deleteStopBookmarks(int index) {
    User user = _currentUserStrategy.getCurrentUser(false);
    if (user == null)
      return;
    _userPropertiesService.deleteStopBookmarks(user, index);
  }

  @Override
  public void setLastSelectedStopIds(List<String> stopIds) {
    User user = _currentUserStrategy.getCurrentUser(true);
    if (user == null)
      return;
    _userPropertiesService.setLastSelectedStopIds(user, stopIds);
  }

  @Override
  public void setRememberUserPreferencesEnabled(
      boolean rememberPreferencesEnabled) {
    User user = _currentUserStrategy.getCurrentUser(true);
    if (user == null)
      return;
    _userPropertiesService.setRememberUserPreferencesEnabled(user,
        rememberPreferencesEnabled);
  }

  @Override
  public String registerPhoneNumber(String phoneNumber) {
    User user = _currentUserStrategy.getCurrentUser(true);
    return _userService.registerPhoneNumber(user, phoneNumber);
  }

  @Override
  public boolean hasPhoneNumberRegistration() {
    IndexedUserDetails details = _currentUserStrategy.getCurrentUserDetails(false);
    if (details == null)
      return false;
    return _userService.hasPhoneNumberRegistration(details.getUserIndexKey());
  }

  @Override
  public boolean completePhoneNumberRegistration(String registrationCode) {

    UserIndex userIndex = _currentUserStrategy.getCurrentUserIndex(false);
    if (userIndex == null)
      return false;

    userIndex = _userService.completePhoneNumberRegistration(userIndex,
        registrationCode);

    if (userIndex == null)
      return false;

    _currentUserStrategy.setCurrentUser(userIndex);

    return true;
  }

  @Override
  public void clearPhoneNumberRegistration() {
    IndexedUserDetails details = _currentUserStrategy.getCurrentUserDetails(false);
    if (details == null)
      return;
    _userService.clearPhoneNumberRegistration(details.getUserIndexKey());
  }

  @Override
  public void markServiceAlertAsRead(String situationId, long time,
      boolean isRead) {
    User user = _currentUserStrategy.getCurrentUser(true);
    if (user == null)
      return;
    _userPropertiesService.markServiceAlertAsRead(user, situationId, time,
        isRead);
  }

  @Override
  public void enableAdminRole() {
    User user = _currentUserStrategy.getCurrentUser(true);
    if (user == null)
      return;
    _userService.enableAdminRoleForUser(user, true);
    _currentUserStrategy.clearCurrentUser();
  }

  @Override
  public void removeUserIndex(UserIndexKey key) {
    UserIndex index = _currentUserStrategy.getCurrentUserIndex(false);
    if (index == null)
      return;
    boolean removingCurrentUserIndex = index.getId().equals(key);
    _userService.removeUserIndexForUser(index.getUser(), key);
    if (removingCurrentUserIndex)
      _currentUserStrategy.clearCurrentUser();
  }

  @Override
  public void deleteCurrentUser() {
    User user = _currentUserStrategy.getCurrentUser(false);
    if (user == null)
      return;
    _userService.deleteUser(user);

    _currentUserStrategy.clearCurrentUser();
  }

  public void resetCurrentUser() {
    User user = _currentUserStrategy.getCurrentUser(false);
    if (user == null)
      return;

    _userService.resetUser(user);
    _currentUserStrategy.clearCurrentUser();
  }

  /****
   * Private Methods
   ****/

  private Mode getModeForRequest(String mode) {
    if (mode == null)
      return Mode.LOGIN;
    if (mode.equals(MODE_REGISTRATION))
      return Mode.REGISTRATION;
    if (mode.equals(MODE_ADD_ACCOUNT))
      return Mode.ADD_ACCOUNT;
    return Mode.LOGIN;
  }

}
