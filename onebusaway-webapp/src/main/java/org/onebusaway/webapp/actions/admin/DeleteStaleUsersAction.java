package org.onebusaway.webapp.actions.admin;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results({@Result(type = "redirectAction", name = "success", params = {
    "actionName", "index"})})
public class DeleteStaleUsersAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private UserService _userService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @SkipValidation
  @Override
  public String execute() {
    _userService.deleteStaleUsers();
    return SUCCESS;
  }
}
