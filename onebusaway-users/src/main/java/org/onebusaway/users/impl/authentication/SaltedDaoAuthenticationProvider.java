/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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
package org.onebusaway.users.impl.authentication;

import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Legacy support for Salted Passwords.
 */
public class SaltedDaoAuthenticationProvider extends DaoAuthenticationProvider {

  private static final Logger _log = LoggerFactory.getLogger(SaltedDaoAuthenticationProvider.class);
  private static final String DEFAULT_VERSION_PREFIX = "v1|";

  private LegacyPasswordEncoder passwordEncoder = new LegacyPasswordEncoder("SHA-256", true);
  private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
  private UserService userService;
  private CurrentUserService currentUserService;
  private String versionPrefix = DEFAULT_VERSION_PREFIX;

  @Autowired
  public void setUserService(UserService userService) {
    this.userService = userService;
  }
  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) { this.currentUserService = currentUserService; }

  public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
    super.setAuthoritiesMapper(authoritiesMapper);
    this.authoritiesMapper = authoritiesMapper;
  }

  public void setVersionPrefix(String prefix) {
    versionPrefix = prefix;
  }

  @Override
  protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
    SaltedUsernamePasswordAuthenticationToken result = new SaltedUsernamePasswordAuthenticationToken(principal, authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
    result.setDetails(authentication.getDetails());
    return result;
  }

  @Override
  public Authentication authenticate(Authentication authentication) {

    Object principal = authentication.getPrincipal();

    SaltedUsernamePasswordAuthenticationToken result = null;

    if (!supports(authentication.getClass()))
      return null;

    result = authenticateNow(authentication);

    if (principal instanceof IndexedUserDetails) {
      result.setDetails(principal);
    } else if (principal instanceof String && result.getDetails() == null) {
      result.setDetails(getUserDetailsService().loadUserByUsername((String)principal));
    }
    return result;

  }

  private SaltedUsernamePasswordAuthenticationToken authenticateNow(Authentication authentication) {
    UserDetails userDetails = null;

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    if (authorities == null || authorities.isEmpty()) {
      Object details = authentication.getDetails();
      if (details != null) {
        if (details instanceof UserDetails) {
          userDetails = (UserDetails)details;
          authorities = userDetails.getAuthorities();
        } else {
          userDetails = getUserDetailsService().loadUserByUsername((String) authentication.getPrincipal());
          authorities = userDetails.getAuthorities();
        }
      }
    }

    if (userDetails == null) {
      _log.error("unable to retrieve user details for user ", authentication.getName());
      return null;
    }

    String username = ((IndexedUserDetails)userDetails).getUserIndexKey().getValue();
    String encPassword = userDetails.getPassword();
    String rawPassword = (String) authentication.getCredentials();

    String proposedPassword = versionPrefix + passwordEncoder.encodePassword(username, rawPassword);


    if (!LegacyMessageDigestPasswordEncoder.PasswordEncoderUtils.equals(proposedPassword, encPassword)) {
      throw new BadCredentialsException("authentication failed for " + username);
    }

    return new SaltedUsernamePasswordAuthenticationToken(
            authentication.getPrincipal(),
            authentication.getCredentials(),
            authoritiesMapper.mapAuthorities(authorities));
  }

  @Override
  public boolean supports(Class authentication) {
    return authentication == SaltedUsernamePasswordAuthenticationToken.class
            || authentication == UsernamePasswordAuthenticationToken.class;
  }

  @Override
  protected void additionalAuthenticationChecks(UserDetails details, UsernamePasswordAuthenticationToken auth)
          throws AuthenticationException {
    super.additionalAuthenticationChecks(details, auth);
  }


}

