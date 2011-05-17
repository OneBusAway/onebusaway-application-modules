package org.onebusaway.webapp.actions.admin;

import java.util.UUID;

import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.onebusaway.webapp.actions.OneBusAwayActionSupport;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ApiKeyGeneratorAction extends OneBusAwayActionSupport {

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
