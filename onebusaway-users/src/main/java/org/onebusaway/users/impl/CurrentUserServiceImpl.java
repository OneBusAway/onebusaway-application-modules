package org.onebusaway.users.impl;

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.impl.authentication.DefaultUserAuthenticationToken;
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
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserServiceImpl implements CurrentUserService {

  private enum Mode {
    LOGIN, REGISTRATION, ADD_ACCOUNT
  };

  private UserService _userService;

  private UserPropertiesService _userPropertiesService;

  private StandardAuthoritiesService _authoritiesService;

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

  /****
   * {@link CurrentUserService} Interface
   ****/

  @Override
  public boolean isCurrentUserAnonymous() {
    IndexedUserDetails details = getCurrentUserDetails();
    if (details == null)
      return true;
    return details.isAnonymous();
  }

  @Override
  public UserBean getCurrentUser() {
    UserIndex userIndex = getCurrentUserIndexInternal();
    if (userIndex == null)
      return null;
    return _userService.getUserAsBean(userIndex.getUser());
  }

  @Override
  public UserIndex getCurrentUserAsUserIndex() {
    return getCurrentUserIndexInternal();
  }

  @Override
  public IndexedUserDetails getIndexedUserDetailsForUser(String type,
      String id, String credentials, boolean isAnonymous, String mode) {
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

      User oldUser = getCurrentUserInternal();
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

    User oldUser = getCurrentUserInternal();
    if (oldUser != null && _userService.isAnonymous(oldUser))
      _userService.mergeUsers(oldUser, index.getUser());

    return new IndexedUserDetailsImpl(_authoritiesService, index);
  }

  @Override
  public IndexedUserDetails handleAddAccount(String type, String id,
      String credentials, boolean isAnonymous) {

    UserIndexKey key = new UserIndexKey(type, id);
    UserIndex index = _userService.getUserIndexForId(key);
    boolean exists = index != null;

    User currentUser = getCurrentUserInternal();
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

    User user = getCurrentUserInternal();
    if (user == null)
      return;
    _userPropertiesService.setDefaultLocation(user, locationName, lat, lon);
  }

  @Override
  public void clearDefaultLocation() {
    User user = getCurrentUserInternal();
    if (user == null)
      return;
    _userPropertiesService.clearDefaultLocation(user);
  }

  @Override
  public int addStopBookmark(String name, List<String> stopIds,
      RouteFilter filter) {
    User user = getCurrentUserInternal();
    if (user == null)
      return -1;
    return _userPropertiesService.addStopBookmark(user, name, stopIds, filter);
  }

  @Override
  public void updateStopBookmark(int id, String name, List<String> stopIds,
      RouteFilter routeFilter) {

    User user = getCurrentUserInternal();
    if (user == null)
      return;
    _userPropertiesService.updateStopBookmark(user, id, name, stopIds,
        routeFilter);
  }

  @Override
  public void deleteStopBookmarks(int index) {
    User user = getCurrentUserInternal();
    if (user == null)
      return;
    _userPropertiesService.deleteStopBookmarks(user, index);
  }

  @Override
  public void setLastSelectedStopIds(List<String> stopIds) {
    User user = getCurrentUserInternal();
    if (user == null)
      return;
    _userPropertiesService.setLastSelectedStopIds(user, stopIds);
  }

  @Override
  public void setRememberUserPreferencesEnabled(
      boolean rememberPreferencesEnabled) {
    User user = getCurrentUserInternal();
    if (user == null)
      return;
    _userPropertiesService.setRememberUserPreferencesEnabled(user,
        rememberPreferencesEnabled);
  }

  @Override
  public String registerPhoneNumber(String phoneNumber) {
    User user = getCurrentUserInternal();
    return _userService.registerPhoneNumber(user, phoneNumber);
  }

  @Override
  public boolean hasPhoneNumberRegistration() {
    IndexedUserDetails details = getCurrentUserDetails();
    if (details == null)
      return false;
    return _userService.hasPhoneNumberRegistration(details.getUserIndexKey());
  }

  @Override
  public boolean completePhoneNumberRegistration(String registrationCode) {

    UserIndex userIndex = getCurrentUserIndexInternal();
    if (userIndex == null)
      return false;

    userIndex = _userService.completePhoneNumberRegistration(userIndex,
        registrationCode);

    if (userIndex == null)
      return false;

    setCurrentUserInternal(userIndex);

    return true;
  }

  @Override
  public void clearPhoneNumberRegistration() {
    IndexedUserDetails details = getCurrentUserDetails();
    if (details == null)
      return;
    _userService.clearPhoneNumberRegistration(details.getUserIndexKey());
  }

  @Override
  public void enableAdminRole() {
    User user = getCurrentUserInternal();
    if (user == null)
      return;
    _userService.enableAdminRoleForUser(user, true);
    clearCurrentUser();
  }

  @Override
  public void removeUserIndex(UserIndexKey key) {
    UserIndex index = getCurrentUserIndexInternal();
    if (index == null)
      return;
    boolean removingCurrentUserIndex = index.getId().equals(key);
    _userService.removeUserIndexForUser(index.getUser(), key);
    if (removingCurrentUserIndex)
      clearCurrentUser();
  }

  @Override
  public void deleteCurrentUser() {
    User user = getCurrentUserInternal();
    if (user == null)
      return;
    _userService.deleteUser(user);

    clearCurrentUser();
  }

  public void resetCurrentUser() {
    User user = getCurrentUserInternal();
    if (user == null)
      return;

    _userService.resetUser(user);

    clearCurrentUser();
  }

  /****
   * Private Methods
   ****/

  private User getCurrentUserInternal() {

    UserIndex userIndex = getCurrentUserIndexInternal();
    if (userIndex == null)
      return null;
    return userIndex.getUser();
  }

  /**
   * We make this package accessible to support testing
   * 
   * @return
   */
  private UserIndex getCurrentUserIndexInternal() {

    IndexedUserDetails details = getCurrentUserDetails();

    if (details == null)
      return null;

    return _userService.getUserIndexForId(details.getUserIndexKey());
  }

  private IndexedUserDetails getCurrentUserDetails() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null)
      return null;

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

  private void setCurrentUserInternal(UserIndex userIndex) {

    IndexedUserDetails userDetails = new IndexedUserDetailsImpl(
        _authoritiesService, userIndex);

    DefaultUserAuthenticationToken token = new DefaultUserAuthenticationToken(
        userDetails);
    SecurityContextHolder.getContext().setAuthentication(token);
  }

  private void clearCurrentUser() {
    // Log out the current user
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  private Mode getModeForRequest(String mode) {
    if (mode == null)
      return Mode.LOGIN;
    if (mode.equals("registration"))
      return Mode.REGISTRATION;
    if (mode.equals("add-account"))
      return Mode.ADD_ACCOUNT;
    return Mode.LOGIN;
  }
}
