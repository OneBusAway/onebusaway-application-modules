package org.onebusaway.presentation.impl.users;

import org.onebusaway.presentation.services.CurrentUserAware;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

import org.springframework.beans.factory.annotation.Autowired;

public class CurrentUserInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 1L;

  private CurrentUserService _currentUserService;

  @Autowired
  public void setCurrentUserService(CurrentUserService userDataService) {
    _currentUserService = userDataService;
  }

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {

    final Object action = invocation.getAction();

    if (action instanceof CurrentUserAware) {
      CurrentUserAware currentUserAware = (CurrentUserAware) action;
      UserBean currentUser = _currentUserService.getCurrentUser();
      if( currentUser != null)
        currentUserAware.setCurrentUser(currentUser);
    }

    return invocation.invoke();
  }
}
