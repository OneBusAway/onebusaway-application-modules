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

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


public class IndexedUserAuthenticationProcessorFilter extends
        UsernamePasswordAuthenticationFilter {

  private UserService _userService;
  private String _indexTypeParameter = "j_indexType";

  public void setIndexTypeParameter(String indexTypeParameter) {
    _indexTypeParameter = indexTypeParameter;
  }

  public String getIndexTypeParameter() {
    return _indexTypeParameter;
  }

  /* during authentication, if you are trying to retrieve the username
  and the user isDisabled in the properties, it will return null
  Without the username, the user can not log in
  */
  @Override
  protected String obtainUsername(HttpServletRequest request) {
    String username = super.obtainUsername(request);
    if (username != null) {
      username = username.trim();
      if (username.length() > 0) {
          username = obtainUserIndexType(request) + "_" + username;
      }
    }
    UserIndex userIndex = _userService.getUserIndexForUsername(username);
    UserBean bean = _userService.getUserAsBean(userIndex.getUser());
    if (bean == null | bean.isDisabled()) {
        return null;
    }
    return username;
  }

  protected String obtainUserIndexType(HttpServletRequest request) {
    return request.getParameter(_indexTypeParameter);
  }

  @Autowired
  public void setUserService(UserService userService) {
        _userService = userService;
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
