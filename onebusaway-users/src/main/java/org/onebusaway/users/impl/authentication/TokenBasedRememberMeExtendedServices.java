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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * Extension of {@link TokenBasedRememberMeServices} that deals with the fact
 * that the cookie token delimiter might be present in usernames
 * 
 * @author bdferris
 */
public class TokenBasedRememberMeExtendedServices extends
    TokenBasedRememberMeServices {

  public TokenBasedRememberMeExtendedServices(String key, UserDetailsService userDetailsService) {
    super(key, userDetailsService);
  }

  @Override
  public UserDetails processAutoLoginCookie(String[] cookieTokens,
      HttpServletRequest request, HttpServletResponse response) {

    if (cookieTokens != null && cookieTokens.length > 3) {
      int n = cookieTokens.length;

      String a = cookieTokens[0];
      for (int i = 1; i < n - 2; i++)
        a += ":" + cookieTokens[i];
      String[] updated = {a, cookieTokens[n - 2], cookieTokens[n - 1]};
      cookieTokens = updated;
    }

    return super.processAutoLoginCookie(cookieTokens, request, response);
  }
}
