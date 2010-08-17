package org.onebusaway.users.impl;

import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class ApiKeyPermissionServiceImpl implements ApiKeyPermissionService {

  private HashMap<String, Long> _lastVisitForUser;
  private UserService _userService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public ApiKeyPermissionServiceImpl() {
    _lastVisitForUser = new HashMap<String, Long>();

  }

  @Override
  public boolean getPermission(String key, String service) {

    Long minRequestInterval = _userService.getMinRequestIntervalForKey(key);
    if (minRequestInterval == null) {
      return false;
    }
    
    long now = System.currentTimeMillis();
    Long lastVisit = _lastVisitForUser.get(key);
    
    boolean ok = false;
    
    if (lastVisit == null || lastVisit + minRequestInterval <= now) {
      ok = true;
    }
    
    _lastVisitForUser.put(key, now);
    return ok;
  }
  
}
