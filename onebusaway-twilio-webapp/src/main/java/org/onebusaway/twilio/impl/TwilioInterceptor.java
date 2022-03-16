/**
 * Copyright (C) 2014 HART (Hillsborough Area Regional Transit) 
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
package org.onebusaway.twilio.impl;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.impl.users.XWorkRequestAttributes;
import org.onebusaway.twilio.services.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import static org.onebusaway.twilio.impl.TwilioDispatchFilter.NEXT_ACTION;

public class TwilioInterceptor extends AbstractInterceptor {

  private static Logger _log = LoggerFactory.getLogger(TwilioInterceptor.class);
        
  private SessionManager _sessionManager;
  private String _phoneNumberParameterName = "From";
  
  public void setPhoneNumberParameterName(String phoneNumberParameterName) {
    _phoneNumberParameterName = phoneNumberParameterName;
  }
  
  @Autowired
  public void setSessionManager(SessionManager sessionManager) {
    _sessionManager = sessionManager;
  }

  
  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    ActionContext context = invocation.getInvocationContext();
    Map<String, Object> parameters = new HashMap<>();
    for (String key : context.getParameters().keySet()) {
      parameters.put(key, context.getParameters().get(key));
    }
    
    /* Stringify parameters for debugging output */
    String paramString = "";  
    for (Entry<String, Object> entry : parameters.entrySet()) {
    	paramString += entry.getKey() + "=";
    	Object val = entry.getValue();
    	if (val instanceof String[]) {
    		paramString += Arrays.toString((String[])val);
    	} else {
    		paramString += val.toString();
    	}
    	paramString += ", ";
    }
    int idx = paramString.lastIndexOf(',');
    if (idx >= 0) {
    	paramString = paramString.substring(0, idx);
    }
    _log.debug("in with params={" + paramString + "} and session=" + context.getSession());

    Object phoneNumber = parameters.get(_phoneNumberParameterName);
    if (phoneNumber == null) {
      return invocation.invoke();
    }

    if (phoneNumber instanceof String[]) {
      String[] values = (String[]) phoneNumber;
      if (values.length == 0)
        return invocation.invoke();
      phoneNumber = values[0];
    }

    String sessionId = phoneNumber.toString();
    // Strip off leading '+', if any
    sessionId = sessionId.replaceFirst("\\+","");
    Map<String, Object> persistentSession = _sessionManager.getContext(sessionId);
    _log.debug("remapping sesssionId " + sessionId + " to " + persistentSession);
    Map<String, Object> originalSession = context.getSession();
    context.setSession(persistentSession);

    String nextAction = (String) context.get(NEXT_ACTION);
    if (nextAction != null) {
      _log.debug("interceptor found nextAction of " + nextAction);
    }

    XWorkRequestAttributes attributes = new XWorkRequestAttributes(context,
        sessionId);
    RequestAttributes originalAttributes = RequestContextHolder.getRequestAttributes();
    RequestContextHolder.setRequestAttributes(attributes);

    Object action = invocation.getAction();
    if (action instanceof SessionAware)
      ((SessionAware) action).setSession(persistentSession);

    try {
      _log.debug("forwarding to " + invocation.getClass().getName() + " via " + action.getClass().getName());
      return invocation.invoke();
    } finally {
      RequestContextHolder.setRequestAttributes(originalAttributes);
      context.setSession(originalSession);
    }
  }
}