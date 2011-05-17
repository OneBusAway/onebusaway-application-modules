package org.onebusaway.webapp.actions.admin;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.model.User;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

@Results( {@Result(type = "redirectAction", params = {
    "actionName", "user-for-id", "id", "${userId}", "parse", "true"})})
public class SetAdminRoleAction extends OneBusAwayActionSupport {

  private static final long serialVersionUID = 1L;

  private int _userId;

  private UserService _userService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public void setUserId(int userId) {
    _userId = userId;
  }

  public int getUserId() {
    return _userId;
  }

  @Override
  public String execute() {
    User user = _userService.getUserForId(_userId);
    if (user == null)
      return INPUT;
    _userService.enableAdminRoleForUser(user, false);
    return SUCCESS;
  }

}
