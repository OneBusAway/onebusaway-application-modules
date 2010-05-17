package org.onebusaway.webapp.actions.user;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;

public class SetRememberPreferencesAction extends AbstractRedirectAction {

  private static final long serialVersionUID = 1L;

  private boolean _enabled;

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  @Override
  @Actions( {
      @Action(value = "/user/set-remember-preferences"),
      @Action(value = "/where/iphone/user/set-remember-preferences"),
      @Action(value = "/where/text/user/set-remember-preferences")})
  public String execute() {
    _currentUserService.setRememberUserPreferencesEnabled(_enabled);
    return SUCCESS;
  }
}
