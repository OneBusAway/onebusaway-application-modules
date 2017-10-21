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
package org.onebusaway.presentation.services.cachecontrol;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.struts2.ServletActionContext;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class CacheControlInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private static Logger _log = LoggerFactory.getLogger(CacheControlInterceptor.class);

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    CacheControl cacheControl = getCacheControlAnnotation(invocation);

    if (cacheControl == null)
      return invocation.invoke();

    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    applyCacheControlHeader(invocation, cacheControl, request, response);
    applyExpiresHeader(invocation, cacheControl, request, response);
    applyETagHeader(invocation, cacheControl, request, response);

    Date lastModifiedTime = applyLastModifiedHeader(invocation, cacheControl,
        request, response);

    Map<String, String> requestCacheControl = parseRequestCacheControl(request);
    boolean bypassCache = bypassCache(requestCacheControl);

    if (lastModifiedTime != null && !bypassCache) {

      String modifiedSinceValue = request.getHeader("if-modified-since");

      if (modifiedSinceValue != null) {
        try {
          Date modifiedSince = DateUtil.parseDate(modifiedSinceValue);
          if (!lastModifiedTime.after(modifiedSince)) {
            response.setStatus(304);
            return null;
          }
        } catch (DateParseException ex) {

        }
      }
    }

    /**
     * If we have a HEAD request and the action has specified a short-circuit
     * response, skip the rest of the action chain
     */
    String method = request.getMethod();
    if (cacheControl.shortCircuit() && "HEAD".equals(method))
      return null;
    
    return invocation.invoke();
  }

  private boolean bypassCache(Map<String, String> requestCacheControl) {
    return requestCacheControl.containsKey("no-cache");
  }

  private Map<String, String> parseRequestCacheControl(
      HttpServletRequest request) {

    String value = request.getHeader("cache-control");
    if (value == null)
      return Collections.emptyMap();

    Map<String, String> m = new HashMap<String, String>();

    for (String kvp : value.split(",")) {
      kvp = kvp.trim();
      if (kvp.length() == 0)
        continue;
      int index = kvp.indexOf('=');
      if (index == -1)
        m.put(kvp.toLowerCase(), null);
      else {
        String key = kvp.substring(0, index).toLowerCase();
        m.put(key, kvp.substring(index + 1));
      }
    }

    return m;
  }

  private void applyCacheControlHeader(ActionInvocation invocation,
      CacheControl cacheControl, HttpServletRequest request,
      HttpServletResponse response) {

    StringBuilder b = new StringBuilder();

    if (cacheControl.isPublic()) {
      delimiter(b);
      b.append("public");
    }

    if (cacheControl.isPrivate()) {
      delimiter(b);
      b.append("private");
    }

    if (cacheControl.noCache()) {
      delimiter(b);
      b.append("no-cache");
    }

    if (cacheControl.noStore()) {
      delimiter(b);
      b.append("no-store");
    }

    if (cacheControl.noTransform()) {
      delimiter(b);
      b.append("no-transform");
    }

    if (cacheControl.maxAge() != -1) {
      delimiter(b);
      b.append("max-age=").append(cacheControl.maxAge());
    }

    if (cacheControl.sharedMaxAge() != -1) {
      delimiter(b);
      b.append("s-maxage=").append(cacheControl.sharedMaxAge());
    }

    if (b.length() > 0) {
      response.setHeader("Cache-Control", b.toString());
    }
  }

  private void applyExpiresHeader(ActionInvocation invocation,
      CacheControl cacheControl, HttpServletRequest request,
      HttpServletResponse response) {

    Date expiresTime = null;

    if (cacheControl.expiresOffset() > 0) {
      expiresTime = new Date(SystemTime.currentTimeMillis()
          + cacheControl.expiresOffset());
    } else {
      expiresTime = invokeDateMethod(invocation, cacheControl.expiresMethod());
    }

    if (expiresTime == null)
      return;

    String expiresTimeAsString = DateUtil.formatDate(expiresTime);
    response.setHeader("Expires", expiresTimeAsString);
  }

  private Date applyLastModifiedHeader(ActionInvocation invocation,
      CacheControl cacheControl, HttpServletRequest request,
      HttpServletResponse response) {

    Date lastModifiedTime = invokeDateMethod(invocation,
        cacheControl.lastModifiedMethod());

    if (lastModifiedTime == null)
      return null;

    String lastModifiedTimeAsString = DateUtil.formatDate(lastModifiedTime);
    response.setHeader("Last-Modified", lastModifiedTimeAsString);

    return lastModifiedTime;
  }

  private void applyETagHeader(ActionInvocation invocation,
      CacheControl cacheControl, HttpServletRequest request,
      HttpServletResponse response) {

    String etagMethod = cacheControl.etagMethod();
    if (etagMethod == null || "".equals(etagMethod))
      return;

    Object action = invocation.getAction();
    Class<?> actionClass = action.getClass();
    Method m = getMethod(actionClass, etagMethod);

    if (m == null) {
      _log.warn("etagMethod=\"" + etagMethod
          + "\" not found for actionClass=\"" + actionClass.getName() + "\"");
      return;
    }

    Object result = invokeMethod(m, action);
    if (result == null)
      return;

    response.setHeader("ETag", result.toString());
  }

  private CacheControl getCacheControlAnnotation(ActionInvocation invocation) {

    Object action = invocation.getAction();
    Class<? extends Object> actionClass = action.getClass();

    ActionProxy proxy = invocation.getProxy();
    String methodName = proxy.getMethod();

    if (methodName != null) {
      try {
        Method m = actionClass.getMethod(methodName);
        if (m != null) {
          CacheControl methodCacheControl = m.getAnnotation(CacheControl.class);
          if (methodCacheControl != null)
            return methodCacheControl;
        }
      } catch (Exception ex) {
        _log.warn("error searching for action method=\"" + methodName
            + "\" on action class=\"" + actionClass.getName() + "\"", ex);
      }
    }

    return actionClass.getAnnotation(CacheControl.class);
  }

  private Date invokeDateMethod(ActionInvocation invocation, String methodName) {

    if (methodName == null || "".equals(methodName))
      return null;

    Object action = invocation.getAction();
    Class<?> actionClass = action.getClass();
    Method m = getMethod(actionClass, methodName);

    if (m == null) {
      _log.warn("method=\"" + methodName + "\" not found for actionClass=\""
          + actionClass.getName() + "\"");
      return null;
    }

    Object result = invokeMethod(m, action);
    if (result == null)
      return null;

    if (result instanceof Date)
      return (Date) result;

    if (result instanceof Long)
      return new Date(((Long) result).longValue());

    _log.warn("unknown date value: " + result + " from method=\"" + methodName
        + "\" in actionClass=" + actionClass.getName());

    return null;
  }

  private static final void delimiter(StringBuilder b) {
    if (b.length() > 0)
      b.append(", ");
  }

  private Method getMethod(Class<?> actionClass, String methodName,
      Class<?>... parameterTypes) {
    try {
      return actionClass.getMethod(methodName, parameterTypes);
    } catch (Exception ex) {
      _log.warn("error searching for action method=\"" + methodName
          + "\" on action class=\"" + actionClass.getName() + "\"", ex);
      return null;
    }
  }

  private Object invokeMethod(Method method, Object target, Object... args) {
    try {
      return method.invoke(target, args);
    } catch (Exception ex) {
      _log.warn("error invoking method", ex);
      return null;
    }
  }

}
