/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

public class EveryLastLoginAuthenticationToken extends AbstractAuthenticationToken
    implements Serializable {

  private static final long serialVersionUID = 1L;

  private int _keyHash;

  private Object _principal;

  private String _credentials;

  public EveryLastLoginAuthenticationToken(String key, Object principal, String credentials, Collection<GrantedAuthority> authorities) {
    super(authorities);

    if ((key == null) || ("".equals(key)) || (principal == null)
        || "".equals(principal) || (authorities == null)
        || (authorities.size() == 0)) {
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
