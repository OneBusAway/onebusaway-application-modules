package org.onebusaway.users.impl;

import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.IndexedUserDetailsService;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserLastAccessTimeService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

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
    _userLastAccessTimeService.handleAccessForUser(user.getId(), System.currentTimeMillis());
  }

  @Override
  public void resetUserForIndexKey(UserIndexKey key) {
    UserIndex index = _userService.getUserIndexForId(key);
    if( index != null)
      _userService.deleteUser(index.getUser());
  }
}
