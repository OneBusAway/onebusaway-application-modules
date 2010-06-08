package org.onebusaway.users.impl.authentication;

import javax.servlet.http.HttpServletRequest;

import org.onebusaway.everylastlogin.server.AuthenticationResult;
import org.onebusaway.everylastlogin.server.LoginManager;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.AbstractProcessingFilter;
import org.springframework.security.ui.FilterChainOrder;

public class EveryLastLoginAuthenticationProcessorFilter extends
    AbstractProcessingFilter {

  private CurrentUserService _currentUserService;

  private boolean _registerIfNewUser = true;

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  public void setRegisterIfNewUser(boolean registerIfNewUser) {
    _registerIfNewUser = registerIfNewUser;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request)
      throws AuthenticationException {

    AuthenticationResult result = LoginManager.getResult(request);
    if (result == null)
      return null;

    IndexedUserDetails details = _currentUserService.handleLogin(
        result.getProvider(), result.getIdentity(), result.getCredentials(),
        _registerIfNewUser);

    if (details == null)
      return null;

    return new DefaultUserAuthenticationToken(details);
  }

  @Override
  public String getDefaultFilterProcessesUrl() {
    return "/everylastlogin_login";
  }

  @Override
  public int getOrder() {
    return FilterChainOrder.AUTHENTICATION_PROCESSING_FILTER;
  }
}
