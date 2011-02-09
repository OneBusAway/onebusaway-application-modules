package org.onebusaway.users.impl;

import java.util.UUID;

import org.onebusaway.users.impl.authentication.DefaultUserAuthenticationToken;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.IndexedUserDetailsService;
import org.onebusaway.users.services.UserIndexTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.rememberme.TokenBasedRememberMeServices;

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
