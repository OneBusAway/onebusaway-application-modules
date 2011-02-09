package org.onebusaway.users.impl;

import org.onebusaway.users.impl.authentication.DefaultUserAuthenticationToken;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

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
