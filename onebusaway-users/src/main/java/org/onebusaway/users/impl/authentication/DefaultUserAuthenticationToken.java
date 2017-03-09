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
import org.springframework.security.core.userdetails.UserDetails;

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
