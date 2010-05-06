package org.onebusaway.webapp.impl;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import java.util.Map;

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

    SessionManagerImpl sessionManager = getSessionManager(context);

    Map<String, Object> session = sessionManager.getContext(phoneNumber.toString());

    Map<String, Object> localSession = context.getSession();
    localSession.putAll(session);

    try {
      return invocation.invoke();
    } finally {
      session.keySet().retainAll(localSession.keySet());
      session.putAll(localSession);
      localSession.clear();
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
