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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onebusaway.everylastlogin.server.AuthenticationResult;
import org.onebusaway.everylastlogin.server.AuthenticationResult.EResultCode;
import org.onebusaway.everylastlogin.server.LoginManager;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@SuppressWarnings("deprecation")
public class EveryLastLoginAuthenticationProcessorFilter extends
        AbstractAuthenticationProcessingFilter {

  protected EveryLastLoginAuthenticationProcessorFilter(
      String defaultFilterProcessesUrl) {
    super(defaultFilterProcessesUrl);
  }

  private CurrentUserService _currentUserService;

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }


  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException, ServletException {

    String mode = request.getParameter("mode");

    AuthenticationResult result = LoginManager.getResult(request);
    if (result == null)
      throw new EveryLastLoginAuthenticationException(
          "AuthenticationResult not found", mode);

    if (result.getCode() != EResultCode.SUCCESS)
      throw new EveryLastLoginAuthenticationException(
          "AuthenticationResult failure", mode);

    IndexedUserDetails details = _currentUserService.handleUserAction(
        result.getProvider(), result.getIdentity(), result.getCredentials(),
        false, mode);

    if (details == null)
      throw new EveryLastLoginAuthenticationException("could not get user details", mode);

    return new DefaultUserAuthenticationToken(details);
  }

  public void setSuccessHandler(AuthenticationSuccessHandler successHandler) {
    super.setAuthenticationSuccessHandler(successHandler);
  }

  public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
    super.setAuthenticationFailureHandler(failureHandler);
  }

  @Override
  public AuthenticationSuccessHandler getSuccessHandler() {
    return super.getSuccessHandler();
  }

  @Override
  public AuthenticationFailureHandler getFailureHandler() {
    return super.getFailureHandler();
  }



}
