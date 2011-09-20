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
package org.onebusaway.webapp.actions.admin;

import java.util.UUID;

import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ApiKeyGeneratorAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private UserService _userService;

  @Autowired
  private UserPropertiesService _propertiesService;
  
  private long _interval;

  private String _apiKey;

  @RequiredFieldValidator
  public void setInterval(long value) {
    _interval = value;
  }
  
  public long getInterval() {
    return _interval;
  }

  public void setApiKey(String apiKey) {
    _apiKey = apiKey;
  }

  public String getApiKey() {
    return _apiKey;
  }

  @Override
  public String execute() {
    if (_apiKey == null) {
      _apiKey = UUID.randomUUID().toString();
    }
    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY, _apiKey);
    
    _userService.getUserIndexForId(key);
    
    UserIndex userIndex = _userService.getOrCreateUserForIndexKey(key, _apiKey,
        false);
    _propertiesService.authorizeApi(userIndex.getUser(), _interval);


    return SUCCESS;
  }
}
