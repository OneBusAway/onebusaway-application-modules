package org.onebusaway.users.impl;

import org.onebusaway.users.model.User;
import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ApiKeyPermissionServiceImpl implements ApiKeyPermissionService {

  private HashMap<User, Long> _lastVisitForUser;
  private UserService _userService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }
  
  public ApiKeyPermissionServiceImpl() {
    _lastVisitForUser = new HashMap<User, Long>();
    
  }
  @Override
  public boolean getPermission(User user, String service) {
    Long minRequestInterval = _userService.getAdditionalPropertyForUser(user, "minRequestInterval");
    if (minRequestInterval == null) {
      return false;
    }
    Long lastVisit = _lastVisitForUser.get(user);
    if (lastVisit == null) {
      return true;
    }
    long now = System.currentTimeMillis();
    return now - lastVisit >= minRequestInterval; 
  }

  @Override
  public void usedKey(User user, String service) {
    Long minRequestInterval = _userService.getAdditionalPropertyForUser(user, "minRequestInterval");
    if (minRequestInterval == null) {
      return ;
    }
    long now = System.currentTimeMillis();
    _lastVisitForUser.put(user, now);
  }

}
