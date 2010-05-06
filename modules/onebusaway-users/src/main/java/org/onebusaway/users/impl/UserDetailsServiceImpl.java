package org.onebusaway.users.impl;

import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.IndexedUserDetailsService;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class UserDetailsServiceImpl implements UserDetailsService,
    IndexedUserDetailsService {

  private UserDao _userDao;

  private StandardAuthoritiesService _authoritiesService;

  @Autowired
  public void setUserDao(UserDao userDao) {
    _userDao = userDao;
  }

  @Autowired
  public void setAuthoritiesService(
      StandardAuthoritiesService authoritiesService) {
    _authoritiesService = authoritiesService;
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

  public IndexedUserDetails getUserForIndexKey(UserIndexKey key)
      throws UsernameNotFoundException, DataAccessException {

    UserIndex userIndex = _userDao.getUserIndexForId(key);

    if (userIndex == null)
      throw new UsernameNotFoundException(key.toString());

    return new IndexedUserDetailsImpl(_authoritiesService, userIndex);
  }

  public IndexedUserDetails getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials) throws DataAccessException {

    UserIndex userIndex = _userDao.getUserIndexForId(key);

    if (userIndex == null) {

      User user = new User();
      user.setCreationTime(new Date());
      user.setTemporary(true);
      user.setProperties(new UserPropertiesV1());
      Set<UserRole> roles = new HashSet<UserRole>();
      roles.add(_authoritiesService.getUserRoleForName(StandardAuthoritiesService.ANONYMOUS));
      user.setRoles(roles);
      _userDao.saveOrUpdateUser(user);

      userIndex = new UserIndex();
      userIndex.setId(key);
      userIndex.setCredentials(credentials);
      userIndex.setUser(user);
      _userDao.saveOrUpdateUserIndex(userIndex);
    }

    return new IndexedUserDetailsImpl(_authoritiesService, userIndex);
  }

}
