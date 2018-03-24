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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

public class LoginAuthenticationSuccessHandler implements
    AuthenticationSuccessHandler {

  private static Logger _log = LoggerFactory.getLogger(LoginAuthenticationSuccessHandler.class);
  
  private static String DEFAULT_TARGET_URL = "/index.action";
  private String targetUrl;
  public void setTargetUrl(String targetUrl) {
    this.targetUrl = targetUrl;
  }
  public String getTargetUrl() {
    return targetUrl;
  }
  
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication success) throws IOException,
      ServletException {
    response.sendRedirect(request.getContextPath() + determineTargetUrl());
  }
  
  protected  String determineTargetUrl() {
    if (targetUrl == null) {
      return DEFAULT_TARGET_URL;
    }

    return targetUrl;
  }

}
