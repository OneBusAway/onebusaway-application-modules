package org.onebusaway.users.impl;

import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateApiKeyAction {

  private UserService _userService;

  private UserPropertiesService _userPropertiesService;

  private String key;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @Autowired
  public void setUserPropertiesService(
      UserPropertiesService userPropertiesService) {
    _userPropertiesService = userPropertiesService;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void execute() {
    UserIndexKey userIndexKey = new UserIndexKey(UserIndexTypes.API_KEY, key);
    UserIndex userIndex = _userService.getOrCreateUserForIndexKey(userIndexKey,
        key, false);
    _userPropertiesService.authorizeApi(userIndex.getUser(), 0);
  }
}
