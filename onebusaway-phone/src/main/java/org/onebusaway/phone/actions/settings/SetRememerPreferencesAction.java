package org.onebusaway.phone.actions.settings;

import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class SetRememerPreferencesAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private CurrentUserService _currentUserService;

  private boolean _enabled;

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService userDataService) {
    _currentUserService = userDataService;
  }

  @Override
  public String execute() {

    _currentUserService.setRememberUserPreferencesEnabled(_enabled);
    return SUCCESS;
  }
}
