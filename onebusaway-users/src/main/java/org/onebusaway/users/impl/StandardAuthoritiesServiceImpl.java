package org.onebusaway.users.impl;

import org.onebusaway.container.spring.PostConstruct;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
class StandardAuthoritiesServiceImpl implements StandardAuthoritiesService {

  private UserDao _userDao;

  private final Map<String, GrantedAuthority> _standardAuthoritiesMap = new ConcurrentHashMap<String, GrantedAuthority>();

  private final Map<String, UserRole> _userRoles = new ConcurrentHashMap<String, UserRole>();

  @Autowired
  public void setUserDao(UserDao userDao) {
    _userDao = userDao;
  }

  @Transactional(readOnly = false)
  @PostConstruct
  public void bootstrap() {
    for (final String auth : STANDARD_AUTHORITIES)
      createStandardAuthority(auth);
  }

  private GrantedAuthority createStandardAuthority(final String name) {

    assert !_standardAuthoritiesMap.containsKey(name);
    assert !_userRoles.containsKey(name);

    UserRole role = _userDao.getUserRoleForName(name);
    if (role == null) {
      role = new UserRole(name);
      _userDao.saveOrUpdateUserRole(role);
    }
    _userRoles.put(name, role);

    final GrantedAuthority auth = new GrantedAuthorityImpl(name);
    _standardAuthoritiesMap.put(name, auth);
    return auth;
  }

  public GrantedAuthority getNameBasedAuthority(final String name) {
    final GrantedAuthority auth = _standardAuthoritiesMap.get(name);
    if (null == auth) {
      return new GrantedAuthorityImpl(name);
    } else {
      return auth;
    }
  }

  public UserRole getUserRoleForName(String name) {
    return _userRoles.get(name);
  }

  @Override
  public UserRole getAdministratorRole() {
    return getUserRoleForName(ADMINISTRATOR);
  }

  @Override
  public UserRole getAnonymousRole() {
    return getUserRoleForName(ANONYMOUS);
  }

  @Override
  public UserRole getUserRole() {
    return getUserRoleForName(USER);
  }
}
