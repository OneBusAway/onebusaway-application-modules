package org.onebusaway.phone.impl;

import java.util.Map;

import org.asteriskjava.fastagi.AgiRequest;
import org.onebusaway.presentation.impl.users.PhoneNumberLoginInterceptor;
import org.onebusaway.probablecalls.AgiEntryPoint;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class IntegrationTestingInterceptor extends AbstractInterceptor {

  public static final String RESET_USER = "oba_integration_test_reset_user";

  private static final long serialVersionUID = 1L;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    ActionContext context = invocation.getInvocationContext();
    AgiRequest request = AgiEntryPoint.getAgiRequest(context);
    Map<?, ?> r = request.getRequest();

    /**
     * This interceptor will be called multiple times in the course of
     * processing the actions for a call, so we only check the reset user param
     * once
     */
    Object value = r.remove(RESET_USER);

    if (value != null && value.equals("true"))
      context.getParameters().put(PhoneNumberLoginInterceptor.RESET_USER,
          Boolean.TRUE);
    return invocation.invoke();
  }

}
