package org.onebusaway.users.impl.authentication;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserAuthenticationProvider implements
    AuthenticationProvider {

  @Override
  public Authentication authenticate(Authentication authentication)
      throws AuthenticationException {

    if (!supports(authentication.getClass()))
      return null;

    return authentication;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean supports(Class authentication) {
    return authentication == DefaultUserAuthenticationToken.class;
  }

}
