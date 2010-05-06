package org.onebusaway.presentation.impl.users;

import java.util.UUID;

import org.springframework.web.context.request.RequestContextHolder;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class RequestContextInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private static final String KEY_SESSION_ID = RequestContextInterceptor.class.getName()
      + ".sessionId";

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    ActionContext context = invocation.getInvocationContext();
    String id = getSessionId(context);

    XWorkRequestAttributes attributes = new XWorkRequestAttributes(context, id);
    RequestContextHolder.setRequestAttributes(attributes);

    String result = invocation.invoke();

    RequestContextHolder.resetRequestAttributes();
    attributes.requestCompleted();

    return result;
  }

  private String getSessionId(ActionContext context) {
    String id = (String) context.getSession().get(KEY_SESSION_ID);
    if (id == null) {
      UUID uuid = UUID.randomUUID();
      id = uuid.toString();
      context.getSession().put(KEY_SESSION_ID, id);
    }
    return id;
  }
}
