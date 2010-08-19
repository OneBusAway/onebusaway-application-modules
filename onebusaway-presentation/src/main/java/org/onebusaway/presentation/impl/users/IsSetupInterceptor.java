package org.onebusaway.presentation.impl.users;

import org.onebusaway.presentation.services.InitialSetupService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Ensures that the webapp has been set up with at least one admin user
 */
public class IsSetupInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private InitialSetupService _initialSetupService;

  @Autowired
  public void setInitialSetupService(InitialSetupService initialSetupService) {
    _initialSetupService = initialSetupService;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    Object action = invocation.getAction();

    Class<? extends Object> actionType = action.getClass();
    SetupAction annotation = actionType.getAnnotation(SetupAction.class);
    
    if( annotation != null)
      return invocation.invoke();

    if (_initialSetupService.isInitialSetupRequired(false)) {
      /* not set up and not trying to; redirect to setup page */
      return "NotSetup";
    }

    return invocation.invoke();
  }

}
