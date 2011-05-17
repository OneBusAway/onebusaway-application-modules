package org.onebusaway.webapp.actions.admin;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;

@Results( {@Result(location = "user.jspx")})
public class UserForIdAction extends OneBusAwayActionSupport implements
    ModelDriven<UserBean> {

  private static final long serialVersionUID = 1L;

  private int _id;

  private UserService _userService;

  private UserBean _user;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public void setId(int id) {
    _id = id;
  }

  @Override
  public UserBean getModel() {
    return _user;
  }

  @Override
  public String execute() {

    User user = _userService.getUserForId(_id);
    
    if (user == null)
      return INPUT;

    _user = _userService.getUserAsBean(user);

    return SUCCESS;
  }
}
