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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class LoginAuthenticationFailureHandler implements
    AuthenticationFailureHandler {

  private static String DEFAULT_FAILURE_URL = "/login.action?failure=true";
  private String failureUrl;
  public void setFailureUrl(String failureUrl) {
    this.failureUrl = failureUrl;
  }
  public String getFailureUrl() {
    return failureUrl;
  }
  
  protected String determineFailureUrl(HttpServletRequest request, AuthenticationException failed) {
    if (failureUrl == null)
      return DEFAULT_FAILURE_URL;
    return failureUrl;
  }
  
  @Override
  public void onAuthenticationFailure(HttpServletRequest request,
      HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    
    String failureUrl = determineFailureUrl(request, failed);
    if (failed instanceof EveryLastLoginAuthenticationException) {
      EveryLastLoginAuthenticationException ex = (EveryLastLoginAuthenticationException) failed;
      String mode = ex.getMode();
      if (mode != null) {
        String prefix = "?";
        if (failureUrl.contains(prefix))
          prefix = "&";
        try {
          failureUrl += prefix + "mode=" + URLEncoder.encode(mode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new IllegalStateException(e);
        }
      }
    }

    response.sendRedirect(request.getContextPath() + failureUrl);
  }

}
