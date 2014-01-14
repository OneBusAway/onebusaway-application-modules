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

import org.apache.commons.lang.ObjectUtils;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@Results({
    @Result(location = "user.jspx"),
    @Result(name = "changePassword", location = "user-for-index-changePassword.jspx"),
    @Result(name = "changePasswordSubmit", type = "redirectAction", params = {
        "actionName", "user-for-index", "type", "${type}", "id", "${id}",
        "parse", "true"})})
public class UserForIndexAction extends ActionSupport implements
    ModelDriven<UserBean> {

  private static final long serialVersionUID = 1L;

  private String _type;

  private String _id;

  private String _password;

  private String _password2;

  private UserService _userService;

  private UserBean _user;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public void setType(String type) {
    _type = type;
  }

  public String getType() {
    return _type;
  }

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public String getUserIndexId() {
    return _id;
  }

  public String getPassword() {
    return _password;
  }

  public void setPassword(String password) {
    _password = password;
  }

  public String getPassword2() {
    return _password2;
  }

  public void setPassword2(String password2) {
    _password2 = password2;
  }

  @Override
  public UserBean getModel() {
    return _user;
  }

  @Override
  public String execute() {

    UserIndexKey key = new UserIndexKey(_type, _id);
    UserIndex userIndex = _userService.getUserIndexForId(key);
    if (userIndex == null)
      return INPUT;

    _user = _userService.getUserAsBean(userIndex.getUser());

    return SUCCESS;
  }

  public String changePassword() {
    String result = execute();
    if (!SUCCESS.equals(result))
      return result;
    return "changePassword";
  }

  public String changePasswordSubmit() {

    UserIndexKey key = new UserIndexKey(_type, _id);
    UserIndex userIndex = _userService.getUserIndexForId(key);
    if (userIndex == null)
      return INPUT;

    if (!ObjectUtils.equals(_password, _password2))
      return INPUT;

    _userService.setPasswordForUsernameUserIndex(userIndex, _password);

    return "changePasswordSubmit";
  }
}
