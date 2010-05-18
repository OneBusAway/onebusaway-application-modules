package org.onebusaway.phone.actions;

import org.onebusaway.users.services.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class RegistrationHandleAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private String _code;

  private CurrentUserService _currentUserService;

  public void setCode(String code) {
    _code = code;
  }

  @Autowired
  public void setCurrentUserService(CurrentUserService currentUserService) {
    _currentUserService = currentUserService;
  }

  @Override
  public String execute() {
    if (_code == null || _code.length() == 0)
      return INPUT;

    if (!_currentUserService.completePhoneNumberRegistration(_code))
      return INPUT;

    return SUCCESS;
  }
}
