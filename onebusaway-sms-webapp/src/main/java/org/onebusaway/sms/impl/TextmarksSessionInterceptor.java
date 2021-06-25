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
package org.onebusaway.sms.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.impl.users.XWorkRequestAttributes;
import org.onebusaway.sms.services.SessionManager;
import org.onebusaway.util.impl.analytics.GoogleAnalyticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.brsanthu.googleanalytics.EventHit;
import com.brsanthu.googleanalytics.PageViewHit;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class TextmarksSessionInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private SessionManager _sessionManager;

  private String _phoneNumberParameterName = "phoneNumber";

  @Autowired
  public void setSessionManager(SessionManager sessionManager) {
    _sessionManager = sessionManager;
  }
  
  @Autowired
  private GoogleAnalyticsServiceImpl _gaService;

  public void setPhoneNumberParameterName(String phoneNumberParameterName) {
    _phoneNumberParameterName = phoneNumberParameterName;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
	
	processGoogleAnalytics();
	
    ActionContext context = invocation.getInvocationContext();
    Map<String, Object> parameters = new HashMap<>();
    for (String key : context.getParameters().keySet()) {
      parameters.put(key, context.getParameters().get(key));
    }

    Object phoneNumber = parameters.get(_phoneNumberParameterName);

    if (phoneNumber == null)
      return invocation.invoke();

    if (phoneNumber instanceof String[]) {
      String[] values = (String[]) phoneNumber;
      if (values.length == 0)
        return invocation.invoke();
      phoneNumber = values[0];
    }

    String sessionId = phoneNumber.toString();
    Map<String, Object> persistentSession = _sessionManager.getContext(sessionId);

    Map<String, Object> originalSession = context.getSession();
    context.setSession(persistentSession);

    XWorkRequestAttributes attributes = new XWorkRequestAttributes(context,
        sessionId);
    RequestAttributes originalAttributes = RequestContextHolder.getRequestAttributes();
    RequestContextHolder.setRequestAttributes(attributes);

    Object action = invocation.getAction();
    if (action instanceof SessionAware)
      ((SessionAware) action).setSession(persistentSession);

    try {
      return invocation.invoke();
    } finally {
      RequestContextHolder.setRequestAttributes(originalAttributes);
      context.setSession(originalSession);
    }
  }
  
  private void processGoogleAnalytics(){
	  processGoogleAnalyticsPageView();
  }
  
  private void processGoogleAnalyticsPageView(){
	  _gaService.post(new PageViewHit());
  }
}
