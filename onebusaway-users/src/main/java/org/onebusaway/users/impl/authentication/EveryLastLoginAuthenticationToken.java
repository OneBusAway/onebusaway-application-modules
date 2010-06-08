package org.onebusaway.users.impl.authentication;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.AbstractAuthenticationToken;

import java.io.Serializable;

public class EveryLastLoginAuthenticationToken extends AbstractAuthenticationToken
    implements Serializable {

  private static final long serialVersionUID = 1L;

  private int _keyHash;

  private Object _principal;

  private String _credentials;

  public EveryLastLoginAuthenticationToken(String key, Object principal, String credentials, GrantedAuthority[] authorities) {
    super(authorities);

    if ((key == null) || ("".equals(key)) || (principal == null)
        || "".equals(principal) || (authorities == null)
        || (authorities.length == 0)) {
      throw new IllegalArgumentException(
          "Cannot pass null or empty values to constructor");
    }

    _keyHash = key.hashCode();
    _principal = principal;
    _credentials = credentials;

    setAuthenticated(true);
  }

  public int getKeyHash() {
    return _keyHash;
  }

  public Object getPrincipal() {
    return _principal;
  }

  public Object getCredentials() {
    return _credentials;
  }

  public boolean equals(Object obj) {

    if (!super.equals(obj))
      return false;

    if (obj instanceof EveryLastLoginAuthenticationToken) {
      EveryLastLoginAuthenticationToken test = (EveryLastLoginAuthenticationToken) obj;

      return this.getKeyHash() == test.getKeyHash();
    }

    return false;
  }

}
