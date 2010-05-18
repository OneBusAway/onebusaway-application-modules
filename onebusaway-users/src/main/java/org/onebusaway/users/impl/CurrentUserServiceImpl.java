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
  public boolean hasCurrentUser() {
    return getCurrentUserInternal() != null;
  }

  @Override
  public UserBean getCurrentUser() {
    User user = getCurrentUserInternal();
    if (user == null)
      return null;
    UserBean bean = _userService.getUserAsBean(user);
    return bean;
  }

  @Override
  public void handleLogin(String type, String id, String credentials) {

    UserIndexKey key = new UserIndexKey(type, id);
    UserIndex index = _userService.getUserIndexForId(key);
    boolean exists = index != null;

    // New user?
    if (!exists) {

      index = _userService.getOrCreateUserForIndexKey(key, credentials, false);
      User newUser = index.getUser();

      User oldUser = getCurrentUserInternal();
      if (_userService.isAnonymous(oldUser))
        _userService.mergeUsers(oldUser, newUser);
    }

    setCurrentUserInternal(index);
  }

  @Override
  public void handleRegistration(String type, String id, String credentials) {
    handleLogin(type, id, credentials);
  }

  @Override
  public void handleAddAccount(String type, String id, String credentials) {

    UserIndexKey key = new UserIndexKey(type, id);
    UserIndex index = _userService.getUserIndexForId(key);
    boolean exists = index != null;

    // New user?
    if (exists) {
      User existingUser = index.getUser();
      User currentUser = getCurrentUserInternal();
      _userService.mergeUsers(existingUser, currentUser);
    } else {
      User currentUser = getCurrentUserInternal();
      index = _userService.addUserIndexToUser(currentUser, key, credentials);
    }

    setCurrentUserInternal(index);
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

    UserIndex userIndex = getCurrentUserIndexInternal(true);
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
    UserIndex index = getCurrentUserIndexInternal(false);
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

    UserIndex userIndex = getCurrentUserIndexInternal(true);
    if (userIndex == null)
      return null;
    return userIndex.getUser();
  }

  /**
   * We make this package accessible to support testing
   * 
   * @return
   */
  private UserIndex getCurrentUserIndexInternal(boolean createIfNeeded) {

    IndexedUserDetails details = getCurrentUserDetails();

    if (details == null)
      return null;

    if (createIfNeeded)
      return _userService.getOrCreateUserForIndexKey(details.getUserIndexKey(),
          details.getPassword(), details.isAnonymous());
    else
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
}
