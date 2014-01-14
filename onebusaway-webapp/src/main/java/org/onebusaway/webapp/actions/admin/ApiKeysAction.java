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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;

@Results({
    @Result(type = "redirectAction", name = "edit", params = {
        "actionName", "api-keys!edit", "apiKey", "${model.apiKey}", "parse",
        "true"}),
    @Result(type = "redirectAction", name = "list", params = {
        "actionName", "api-keys"})})
public class ApiKeysAction extends ActionSupport implements
    ModelDriven<ApiKeyModel> {

  private static final long serialVersionUID = 1L;

  private UserService _userService;

  private UserPropertiesService _userPropertiesService;

  private List<String> _apiKeys;

  private ApiKeyModel _model = new ApiKeyModel();

  private String _data;

  private File _file;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @Autowired
  public void setUserPropertiesService(
      UserPropertiesService userPropertiesService) {
    _userPropertiesService = userPropertiesService;
  }

  @Override
  public ApiKeyModel getModel() {
    return _model;
  }

  public List<String> getApiKeys() {
    return _apiKeys;
  }

  public void setData(String data) {
    _data = data;
  }

  public void setFile(File file) {
    _file = file;
  }

  @Override
  @SkipValidation
  public String execute() {
    _apiKeys = _userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY);
    return SUCCESS;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "model.apiKey", message = "Hey!")})
  public String edit() {
    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY,
        _model.getApiKey());
    UserIndex userIndex = _userService.getUserIndexForId(key);
    if (userIndex == null)
      return INPUT;
    UserBean bean = _userService.getUserAsBean(userIndex.getUser());
    _model.setMinApiRequestInterval(bean.getMinApiRequestInterval());

    _apiKeys = _userService.getUserIndexKeyValuesForKeyType(UserIndexTypes.API_KEY);

    return SUCCESS;
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "model.apiKey", message = "Hey!")})
  public String saveOrUpdate() {

    saveOrUpdateKey(_model.getApiKey(), _model.getMinApiRequestInterval());
    return "edit";
  }

  @SkipValidation
  public String bulkSaveOrUpdate() throws IOException {

    if (_data != null)
      saveOrUpdateApiKeysFromReader(new StringReader(_data));

    if (_file != null)
      saveOrUpdateApiKeysFromReader(new FileReader(_file));

    return "list";
  }

  @Validations(requiredStrings = {@RequiredStringValidator(fieldName = "model.apiKey", message = "Hey!")})
  public String delete() {

    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY,
        _model.getApiKey());
    UserIndex userIndex = _userService.getUserIndexForId(key);
    if (userIndex == null)
      return INPUT;

    User user = userIndex.getUser();

    _userService.removeUserIndexForUser(user, key);

    if (user.getUserIndices().isEmpty())
      _userService.deleteUser(user);

    // Clear the cached value here
    _userService.getMinApiRequestIntervalForKey(_model.getApiKey(), true);

    return "list";
  }

  @SkipValidation
  public String generate() {
    _model.setApiKey(UUID.randomUUID().toString());
    _model.setMinApiRequestInterval(100L);
    return saveOrUpdate();
  }

  /****
   * Private Methods
   ****/

  private void saveOrUpdateKey(String apiKey, Long minApiRequestInterval) {

    UserIndexKey key = new UserIndexKey(UserIndexTypes.API_KEY, apiKey);
    UserIndex userIndex = _userService.getOrCreateUserForIndexKey(key, "", true);

    _userPropertiesService.authorizeApi(userIndex.getUser(),
        minApiRequestInterval);

    // Clear the cached value here
    _userService.getMinApiRequestIntervalForKey(apiKey, true);
  }

  private void saveOrUpdateApiKeysFromReader(Reader in) throws IOException {

    BufferedReader reader = new BufferedReader(in);
    String line = null;

    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split(",");
      String apiKey = tokens[0];
      Long minApiRequestInterval = Long.parseLong(tokens[1]);
      saveOrUpdateKey(apiKey, minApiRequestInterval);
    }

    reader.close();
  }

}
