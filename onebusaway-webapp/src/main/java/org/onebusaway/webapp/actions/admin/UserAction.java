package org.onebusaway.webapp.actions.admin;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class UserAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private String _type;

  private String _id;

  private UserService _userService;

  private UserBean _user;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public void setType(String type) {
    _type = type;
  }

  public void setId(String id) {
    _id = id;
  }

  public UserBean getUser() {
    return _user;
  }

  @Override
  public String execute() {

    UserIndexKey key = new UserIndexKey(_type, _id);
    UserIndex userIndex = _userService.getUserIndexForId(key);
    if (userIndex == null)
      return INPUT;

    _user = _userService.getUserAsBean(userIndex.getUser());

    return SUCCESS;
  }
}
