package org.onebusaway.users.impl;

import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.springframework.security.GrantedAuthority;

public class MockStandardAuthoritiesServiceImpl implements
    StandardAuthoritiesService {

  private UserRole _admin = new UserRole(ADMINISTRATOR);

  private UserRole _anonymous = new UserRole(ANONYMOUS);

  private UserRole _user = new UserRole(USER);

  @Override
  public UserRole getAdministratorRole() {
    return _admin;
  }

  @Override
  public UserRole getAnonymousRole() {
    return _anonymous;
  }

  @Override
  public UserRole getUserRole() {
    return _user;
  }

  @Override
  public GrantedAuthority getNameBasedAuthority(String name) {
    return null;
  }

  @Override
  public UserRole getUserRoleForName(String name) {
    return null;
  }

}
