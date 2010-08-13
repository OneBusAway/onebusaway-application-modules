package org.onebusaway.webapp.actions.admin;

import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.onebusaway.users.services.validation.KeyValidationService;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class ApiKeyGeneratorAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  @Autowired
  private KeyValidationService _validationService;

  @Autowired
  private UserService _userService;

  @Autowired
  private UserPropertiesService _propertiesService;
  
  private long _interval;


  @RequiredFieldValidator
  public void setInterval(long value) {
    _interval = value;
  }
  
  public long getValue() {
    return _interval;
  }

  @Override
  public String execute() {
    String apiKey = _validationService.generateKeyWithDefaultProvider("");;
    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY, apiKey);
    
    _userService.getUserIndexForId(key);
    
    UserIndex userIndex = _userService.getOrCreateUserForIndexKey(key, apiKey, false);
    _propertiesService.authorizeApi(userIndex.getUser(), _interval);

    return SUCCESS;
  }
}
