package org.onebusaway.webapp.actions;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.everylastlogin.server.AuthenticationResult;
import org.onebusaway.everylastlogin.server.LoginManager;
import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results( {@Result(type = "redirectAction", params = {
    "actionName", "/user/index"})})
public class AddAccountHandlerAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private CurrentUserService _currentUserService;

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  @Override
  public String execute() {
    HttpServletRequest request = ServletActionContext.getRequest();

    AuthenticationResult result = LoginManager.getResult(request);

    if (result == null)
      return INPUT;

    switch (result.getCode()) {
      case SUCCESS:
        _currentUserService.handleAddAccount(result.getProvider(),
            result.getIdentity(), result.getCredentials(), false);
        return SUCCESS;
      case NO_SUCH_PROVIDER:
      case AUTHENTICATION_FAILED:
      default:
        return INPUT;
    }
  }
}
