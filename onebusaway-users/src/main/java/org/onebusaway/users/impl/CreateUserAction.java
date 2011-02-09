package org.onebusaway.users.impl;

import javax.annotation.PostConstruct;

import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateUserAction {

  private UserService _userService;

  private String username;

  private String password;

  private boolean admin;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  @PostConstruct
  public void execute() {

    UserIndex userIndex = _userService.getOrCreateUserForUsernameAndPassword(
        username, password);

    if (userIndex == null)
      throw new IllegalStateException("error creating user");

    if (admin) {
      User user = userIndex.getUser();
      _userService.enableAdminRoleForUser(user, false);
    }
  }
}
