package org.onebusaway.webapp.actions.admin;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results( {@Result(type = "redirectAction", name = "userCreated", params = {
    "actionName", "user-for-id", "id", "${userId}", "parse", "true"})})
public class CreateLoginAction extends OneBusAwayActionSupport {

  private static final long serialVersionUID = 1L;

  private PasswordEncoder _passwordEncoder;

  private UserService _userService;

  private String _userName;

  private String _password;

  private String _password2;

  private int _userId;

  @Autowired
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    _passwordEncoder = passwordEncoder;
  }

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public void setUserName(String userName) {
    _userName = userName;
  }

  public String getUserName() {
    return _userName;
  }

  public void setPassword(String password) {
    _password = password;
  }

  public String getPassword() {
    return _password;
  }

  public void setPassword2(String password2) {
    _password2 = password2;
  }

  public String getPassword2() {
    return _password2;
  }

  public int getUserId() {
    return _userId;
  }

  @SkipValidation
  @Override
  public String execute() {
    return SUCCESS;
  }

  @Validations(requiredStrings = {
      @RequiredStringValidator(fieldName = "userName", key = ""),
      @RequiredStringValidator(fieldName = "password", key = ""),
      @RequiredStringValidator(fieldName = "password2", key = "")})
  public String submit() {

    if (!_password.equals(_password2))
      return INPUT;

    String credentials = _passwordEncoder.encodePassword(_password, _userName);
    UserIndexKey key = new UserIndexKey(UserIndexTypes.USERNAME, _userName);
    UserIndex userIndex = _userService.getOrCreateUserForIndexKey(key,
        credentials, false);

    if (userIndex == null)
      return null;

    _userId = userIndex.getUser().getId();

    return "userCreated";
  }
}
