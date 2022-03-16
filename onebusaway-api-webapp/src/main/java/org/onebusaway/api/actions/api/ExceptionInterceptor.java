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

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.api.ResponseCodes;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.exceptions.NoSuchRouteServiceException;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.NoSuchTripServiceException;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.apache.struts2.rest.ContentTypeHandlerManager;
import org.apache.struts2.rest.DefaultHttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionInterceptor extends AbstractInterceptor {

  private static Logger _log = LoggerFactory.getLogger(ExceptionInterceptor.class);

  private static final long serialVersionUID = 1L;

  private static final int V1 = 1;

  private ContentTypeHandlerManager _handlerSelector;

  @Inject
  public void setMimeTypeHandlerSelector(ContentTypeHandlerManager sel) {
    _handlerSelector = sel;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    try {
      return invocation.invoke();
    } catch (Exception ex) {
      ActionProxy proxy = invocation.getProxy();
      ResponseBean response = getExceptionAsResponseBean(invocation, ex);
      DefaultHttpHeaders methodResult = new DefaultHttpHeaders().withStatus(response.getCode());
      return _handlerSelector.handleResult(proxy.getConfig(), methodResult,
          response);
    }
  }


  protected ResponseBean getExceptionAsResponseBean(ActionInvocation invocation, Exception ex) {
    if (ex instanceof NoSuchStopServiceException
        || ex instanceof NoSuchTripServiceException
        || ex instanceof NoSuchRouteServiceException) {
      return new ResponseBean(V1, ResponseCodes.RESPONSE_RESOURCE_NOT_FOUND,
          ex.getMessage(), null);
    }
    else if( ex instanceof OutOfServiceAreaServiceException) {
      return new ResponseBean(V1, ResponseCodes.RESPONSE_OUT_OF_SERVICE_AREA,
          ex.getMessage(), null);
    }
    else {
      String url = getActionAsUrl(invocation);
      _log.warn("exception for action: url=" + url, ex);
      return new ResponseBean(V1, ResponseCodes.RESPONSE_SERVICE_EXCEPTION,
          ex.getMessage(), null);
    }
  }

  private String getActionAsUrl(ActionInvocation invocation) {

    ActionProxy proxy = invocation.getProxy();
    ActionContext context = invocation.getInvocationContext();

    StringBuilder b = new StringBuilder();
    b.append(proxy.getNamespace());
    b.append("/");
    b.append(proxy.getActionName());
    b.append("!");
    b.append(proxy.getMethod());

    Map<String, Object> params = new HashMap<>();
    for (String key : context.getParameters().keySet()) {
      params.put(key, context.getParameters().get(key).getValue());
    }

    if (!params.isEmpty()) {
      b.append("?");
      boolean seenFirst = false;
      for (Map.Entry<String, Object> entry : params.entrySet()) {

        // Prune out any identifying information
        if ("app_uid".equals(entry.getKey()))
          continue;

        Object value = entry.getValue();
        String[] values = (value instanceof String[]) ? (String[]) value
            : new String[] {value.toString()};
        for (String v : values) {
          if (seenFirst)
            b.append("&");
          else
            seenFirst = true;
          b.append(entry.getKey());
          b.append("=");
          b.append(v);
        }
      }
    }

    return b.toString();
  }
}
