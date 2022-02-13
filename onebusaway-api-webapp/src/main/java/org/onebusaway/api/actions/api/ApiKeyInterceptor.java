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
package org.onebusaway.api.actions.api;

import org.apache.struts2.dispatcher.Parameter;
import org.onebusaway.api.ResponseCodes;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.users.services.ApiKeyPermissionService;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.struts2.rest.ContentTypeHandlerManager;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

/**
 * Ensures that there is a valid API key for the API request, and that a given API key
 * is not used more frequently than it is permitted to.
 */
public class ApiKeyInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  @Autowired
  private ApiKeyPermissionService _keyService;
  
  private ContentTypeHandlerManager _handlerSelector;

  @Inject
  public void setMimeTypeHandlerSelector(ContentTypeHandlerManager sel) {
    _handlerSelector = sel;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    Object action = invocation.getAction();
    Class<? extends Object> actionType = action.getClass();
    ApiKeyAuthorization annotation = actionType.getAnnotation(ApiKeyAuthorization.class);

    if (annotation != null) {
      if (!annotation.enabled())
        return invocation.invoke();
    }

    ApiKeyPermissionService.Status allowed = isAllowed(invocation);
    
    if (ApiKeyPermissionService.Status.AUTHORIZED != allowed) {
      //this user is not authorized to use the API, at least for now
      return unauthorized(invocation, allowed);
    }
        
    return invocation.invoke();
  }

  private ApiKeyPermissionService.Status isAllowed(ActionInvocation invocation) {
    ActionContext context = invocation.getInvocationContext();
    Parameter key = context.getParameters().get("key");
    String[] keys = (String[]) key.getObject();
    
    if( keys == null || keys.length == 0)
      return ApiKeyPermissionService.Status.UNAUTHORIZED;

    return _keyService.getPermission(keys[0], "api");
  }

  // package private for unit tests
  String unauthorized(ActionInvocation invocation, ApiKeyPermissionService.Status reason) throws IOException {
    ActionProxy proxy = invocation.getProxy();
    int httpCode = ResponseCodes.RESPONSE_UNAUTHORIZED;
    String message = "permission denied";
    switch (reason) {
      case UNAUTHORIZED:
        httpCode = ResponseCodes.RESPONSE_UNAUTHORIZED;
        break;
      case  RATE_EXCEEDED:
        httpCode = ResponseCodes.RESPONSE_TOO_MANY_REQUESTS;
        message = "rate limit exceeded";
        break;
      case  AUTHORIZED:
        // this should never happen!
        throw new IllegalStateException("Valid status code " + reason + " in unauthorized response");
      default:
        httpCode = ResponseCodes.RESPONSE_UNAUTHORIZED;
    }
    
    ResponseBean response = new ResponseBean(1, httpCode, message, null);
    DefaultHttpHeaders methodResult = new DefaultHttpHeaders().withStatus(response.getCode());
    return _handlerSelector.handleResult(proxy.getInvocation(), methodResult, response);
  }

  @Autowired
  public void setKeyService(ApiKeyPermissionService _keyService) {
    this._keyService = _keyService;
  }

  public ApiKeyPermissionService getKeyService() {
    return _keyService;
  }

}
