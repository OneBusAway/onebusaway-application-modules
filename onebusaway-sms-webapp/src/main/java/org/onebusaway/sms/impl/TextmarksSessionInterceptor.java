package org.onebusaway.sms.impl;

import java.util.Map;

import org.apache.struts2.interceptor.SessionAware;
import org.onebusaway.presentation.impl.users.XWorkRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class TextmarksSessionInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private static final String SESSION_KEY = TextmarksSessionInterceptor.class.getName()
      + ".sessionKey";

  private String _phoneNumberParameterName = "phoneNumber";

  public void setPhoneNumberParameterName(String phoneNumberParameterName) {
    _phoneNumberParameterName = phoneNumberParameterName;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    ActionContext context = invocation.getInvocationContext();
    Map<String, Object> parameters = context.getParameters();

    Object phoneNumber = parameters.get(_phoneNumberParameterName);

    if (phoneNumber == null)
      return invocation.invoke();
    
    if( phoneNumber instanceof String[]) {
      String[] values = (String[]) phoneNumber;
      if( values.length == 0)
        return invocation.invoke();
      phoneNumber = values[0];
    }

    SessionManagerImpl sessionManager = getSessionManager(context);

    String sessionId = phoneNumber.toString();
    Map<String, Object> persistentSession = sessionManager.getContext(sessionId);
    
    Map<String, Object> originalSession = context.getSession();
    context.setSession(persistentSession);
    
    
    
    XWorkRequestAttributes attributes = new XWorkRequestAttributes(context, sessionId);
    RequestAttributes originalAttributes = RequestContextHolder.getRequestAttributes();
    RequestContextHolder.setRequestAttributes(attributes);
    
    Object action = invocation.getAction();
    if( action instanceof SessionAware )
      ((SessionAware) action).setSession(persistentSession);
    
    try {
      return invocation.invoke();
    } finally {
      RequestContextHolder.setRequestAttributes(originalAttributes);
      context.setSession(originalSession);
    }
  }

  private SessionManagerImpl getSessionManager(ActionContext context) {
    Map<String, Object> application = context.getApplication();
    synchronized (SESSION_KEY) {
      SessionManagerImpl sessionManager = (SessionManagerImpl) application.get(SESSION_KEY);
      if (sessionManager == null) {
        sessionManager = new SessionManagerImpl();
        application.put(SESSION_KEY, sessionManager);
      }
      return sessionManager;
    }
  }
}
