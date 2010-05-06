package org.onebusaway.users.services;

import org.onebusaway.users.model.UserRole;

import org.springframework.security.GrantedAuthority;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface StandardAuthoritiesService {

  public final static String ANONYMOUS = "ROLE_ANONYMOUS";

  public final static String USER = "ROLE_USER";

  public final static String ADMINISTRATOR = "ROLE_ADMINISTRATOR";

  /**
   * This should be the only place where the standard authorities for the app
   * are defined
   */
  public final static List<String> STANDARD_AUTHORITIES = Collections.unmodifiableList(Arrays.asList(
      ADMINISTRATOR, USER, ANONYMOUS));

  /**
   * Get the GrantedAuthority used by Spring Security for the role by name
   * 
   * @param name Role name like {@link #USER}
   * @return Spring Security authority
   */
  public GrantedAuthority getNameBasedAuthority(String name);
  
  public UserRole getUserRoleForName(String name);
}
