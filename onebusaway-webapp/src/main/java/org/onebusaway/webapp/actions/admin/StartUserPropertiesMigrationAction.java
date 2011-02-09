package org.onebusaway.webapp.actions.admin;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.services.UserPropertiesMigrationStatus;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results( {@Result(type = "redirectAction", params = {"actionName", "user-properties-migration"})})
public class StartUserPropertiesMigrationAction extends ActionSupport {

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
    _userService.startUserPropertiesMigration();
    return SUCCESS;
  }
}
