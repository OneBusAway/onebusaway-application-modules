package org.onebusaway.webapp.actions.user;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class IndexAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private CurrentUserService _currentUserService;

  private UserBean _user;

  @Autowired
  public void setCurrentUserService(CurrentUserService userDataService) {
    _currentUserService = userDataService;
  }

  public UserBean getUser() {
    return _user;
  }

  @Override
  @Actions( {
      @Action(value = "/user/index"),
      @Action(value = "/where/iphone/user/index"),
      @Action(value = "/where/text/user/index")})
  public String execute() {
    _user = _currentUserService.getCurrentUser();
    return SUCCESS;
  }
}
