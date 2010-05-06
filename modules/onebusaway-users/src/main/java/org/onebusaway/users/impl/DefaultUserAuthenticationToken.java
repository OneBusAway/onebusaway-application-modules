package org.onebusaway.users.impl;

import org.springframework.security.providers.AbstractAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;

import java.io.Serializable;

public class DefaultUserAuthenticationToken extends AbstractAuthenticationToken
    implements Serializable {

  private static final long serialVersionUID = 1L;

  private String _credentials;

  public DefaultUserAuthenticationToken(UserDetails details) {
    super(details.getAuthorities());
    super.setDetails(details);
    _credentials = details.getPassword();
  }

  public Object getPrincipal() {
    return getDetails();
  }

  public Object getCredentials() {
    return _credentials;
  }
}
