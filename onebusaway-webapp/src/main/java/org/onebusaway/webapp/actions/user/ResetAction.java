package org.onebusaway.webapp.actions.user;

import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

public class ResetAction extends AbstractRedirectAction {

  private static final long serialVersionUID = 1L;

  private CurrentUserService _currentUserService;

  @Autowired
  public void setCurrentUserService(CurrentUserService userDataService) {
    _currentUserService = userDataService;
  }

  @Override
  public String execute() {
    _currentUserService.resetCurrentUser();
    return SUCCESS;
  }
}
