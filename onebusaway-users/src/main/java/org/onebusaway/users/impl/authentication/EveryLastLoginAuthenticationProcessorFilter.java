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

  private enum Mode {
    LOGIN, REGISTRATION, ADD_ACCOUNT
  }

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

    Mode mode = getModeForRequest(request);

    IndexedUserDetails details = applyAuthenticationResultForMode(result, mode);

    if (details == null)
      return null;

    return new DefaultUserAuthenticationToken(details);
  }

  private IndexedUserDetails applyAuthenticationResultForMode(
      AuthenticationResult result, Mode mode) {
    switch (mode) {
      case LOGIN:
        return _currentUserService.handleLogin(result.getProvider(),
            result.getIdentity(), result.getCredentials(), false,
            _registerIfNewUser);
      case REGISTRATION:
        return _currentUserService.handleRegistration(result.getProvider(),
            result.getIdentity(), result.getCredentials(), false);
      case ADD_ACCOUNT:
        return _currentUserService.handleAddAccount(result.getProvider(),
            result.getIdentity(), result.getCredentials(), false);
    }
    throw new IllegalStateException("unknown mode=" + mode);
  }

  @Override
  public String getDefaultFilterProcessesUrl() {
    return "/everylastlogin_login";
  }

  @Override
  public int getOrder() {
    return FilterChainOrder.AUTHENTICATION_PROCESSING_FILTER;
  }

  private Mode getModeForRequest(HttpServletRequest request) {
    String mode = request.getParameter("mode");
    if (mode == null)
      return Mode.LOGIN;
    if (mode.equals("registration"))
      return Mode.REGISTRATION;
    if (mode.equals("add-account"))
      return Mode.ADD_ACCOUNT;
    return Mode.LOGIN;
  }
}
