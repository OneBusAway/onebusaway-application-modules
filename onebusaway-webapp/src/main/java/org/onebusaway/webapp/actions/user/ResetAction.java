package org.onebusaway.webapp.actions.user;

public class ResetAction extends AbstractRedirectAction {

  private static final long serialVersionUID = 1L;

  @Override
  public String execute() {
    _currentUserService.resetCurrentUser();
    return SUCCESS;
  }
}
