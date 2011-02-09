package org.onebusaway.webapp.actions.user;


public class EnableAdminRoleAction extends AbstractRedirectAction {

  private static final long serialVersionUID = 1L;
  
  @Override
  public String execute() {
    _currentUserService.enableAdminRole();
    return SUCCESS;
  }

}
