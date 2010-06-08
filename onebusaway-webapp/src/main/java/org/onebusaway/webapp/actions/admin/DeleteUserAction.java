package org.onebusaway.webapp.actions.admin;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.CurrentUserService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results( {@Result(type = "redirectAction", params = {"actionName", "index"})})
public class DeleteUserAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private UserService _userService;

  private CurrentUserService _currentUserService;

  private int _userId;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  public void setUserId(int userId) {
    _userId = userId;
  }

  @Override
  public String execute() {

    User user = _userService.getUserForId(_userId);

    if (user == null)
      return INPUT;

    UserIndex currentUserIndex = _currentUserService.getCurrentUserAsUserIndex(false);
    if( currentUserIndex != null) {
      User currentUser = currentUserIndex.getUser();
      if( currentUser.equals(user))
        return ERROR;
    }
    _userService.deleteUser(user);

    return SUCCESS;
  }
}
