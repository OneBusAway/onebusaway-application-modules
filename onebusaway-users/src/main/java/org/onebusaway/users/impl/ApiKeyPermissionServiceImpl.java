/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.users.impl;

import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.UserService;
import org.onebusaway.util.SystemTime;
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
  public Status getPermission(String key, String service) {

    Long minRequestInterval = _userService.getMinApiRequestIntervalForKey(key,false);
    if (minRequestInterval == null) {
      return Status.UNAUTHORIZED;
    }
    
    long now = SystemTime.currentTimeMillis();
    Long lastVisit = _lastVisitForUser.get(key);
    
    Status ok = Status.RATE_EXCEEDED;
    
    if (lastVisit == null || lastVisit + minRequestInterval <= now) {
      ok = Status.AUTHORIZED;
    }
    
    _lastVisitForUser.put(key, now);
    return ok;
  }
  
}
