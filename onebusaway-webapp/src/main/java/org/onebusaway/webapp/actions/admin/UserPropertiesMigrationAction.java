package org.onebusaway.webapp.actions.admin;

import org.onebusaway.users.services.UserPropertiesMigrationStatus;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

public class UserPropertiesMigrationAction extends OneBusAwayActionSupport {

  private static final long serialVersionUID = 1L;

  private UserService _userService;

  private UserPropertiesMigrationStatus _status;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public UserPropertiesMigrationStatus getStatus() {
    return _status;
  }

  @Override
  public String execute() {
    _status = _userService.getUserPropertiesMigrationStatus();
    return SUCCESS;
  }
}
