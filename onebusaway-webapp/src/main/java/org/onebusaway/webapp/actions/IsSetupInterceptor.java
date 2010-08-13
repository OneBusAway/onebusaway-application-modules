package org.onebusaway.webapp.actions;

import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.setup.SetupAction;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Ensures that the webapp has been set up with at least one admin user
 */
public class IsSetupInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  @Autowired
  private UserService _userService;

  private StandardAuthoritiesService _authoritiesService;

  @Autowired
  public void setAuthoritiesService(
      StandardAuthoritiesService authoritiesService) {
    _authoritiesService = authoritiesService;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    Object action = invocation.getAction();
    Class<? extends Object> actionType = action.getClass();
    
    if (actionType == SetupAction.class || actionType.isAssignableFrom(ResourcesAction.class)) {
      return invocation.invoke();
    }

    UserRole admin = _authoritiesService.getAdministratorRole();

    int nUsers = _userService.getNumberOfUsersForRole(admin);
    
    if (nUsers == 0) {
      /* not set up and not trying to; redirect to setup page */

      return "NotSetup";
    }
    
    return invocation.invoke();
  }

}
