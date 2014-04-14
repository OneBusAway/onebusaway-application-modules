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
package org.onebusaway.users.impl;

import java.util.UUID;

import org.onebusaway.users.impl.authentication.DefaultUserAuthenticationToken;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.IndexedUserDetailsService;
import org.onebusaway.users.services.UserIndexTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

public class AutoUserCurrentUserStrategyImpl extends CurrentUserStrategyImpl {

  private TokenBasedRememberMeServices _rememberMeServices;

  private IndexedUserDetailsService _userDetailsService;

  @Autowired
  public void setRememberMeServices(
      TokenBasedRememberMeServices rememberMeServices) {
    _rememberMeServices = rememberMeServices;
  }

  @Autowired
  public void setUserDetailsService(IndexedUserDetailsService userDetailsService) {
    _userDetailsService = userDetailsService;
  }

  @Override
  public IndexedUserDetails getCurrentUserDetails(
      boolean createUserIfAppropriate) {

    IndexedUserDetails details = super.getCurrentUserDetails(createUserIfAppropriate);

    if (details == null && createUserIfAppropriate) {
      
      Authentication authentication = createAuthentication();
      details = getUserDetailsForAuthentication(authentication);
      
      SecurityContextHolder.getContext().setAuthentication(authentication);
      RequestAndResponseContext context = RequestAndResponseContext.getContext();
      if (context != null)
        _rememberMeServices.onLoginSuccess(context.getRequest(),
            context.getResponse(), authentication);
    }

    return details;
  }

  protected Authentication createAuthentication() {

    UUID uuid = UUID.randomUUID();
    UUID credentials = UUID.randomUUID();

    UserIndexKey principal = new UserIndexKey(UserIndexTypes.WEB,
        uuid.toString());
    IndexedUserDetails details = _userDetailsService.getOrCreateUserForIndexKey(
        principal, credentials.toString(), true);

    return new DefaultUserAuthenticationToken(details);
  }
}
