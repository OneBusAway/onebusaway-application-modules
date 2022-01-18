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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class SaltedUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

  private Object principal;
  private Object credentials;
  private CurrentUserService userService;

  SaltedUsernamePasswordAuthenticationToken(Object principal, Object credentials,
                                            Collection<? extends GrantedAuthority> authorities) {
    super(principal, credentials, authorities);
    this.principal = principal;
    this.credentials = credentials;
  }

  @Override
  public Object getCredentials() {
    String salt = null;
    if (principal instanceof  IndexedUserDetails) {
      IndexedUserDetails userDetails = (IndexedUserDetails) this.principal;
      salt = userDetails.getUsername();
    } else if (principal instanceof String) {
      salt = (String) this.principal;
    }
    SaltedSha256PasswordEncoder encoder = new SaltedSha256PasswordEncoder("SHA");
    return encoder.encodePassword(credentials.toString(), salt);

  }

} // end Salted class
