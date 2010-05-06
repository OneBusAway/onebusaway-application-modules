package org.onebusaway.webapp.actions.admin;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.IndexedUserDetailsService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

public class UserAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private IndexedUserDetailsService _userIndexService;

  private String _type;

  private String _id;

  private UserService _userService;

  private UserBean _user;

  @Autowired
  public void setIndexedUserDetailsService(IndexedUserDetailsService service) {
    _userIndexService = service;
  }

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  public void setType(String type) {
    _type = type;
  }

  public void setId(String id) {
    _id = id;
  }

  public UserBean getUser() {
    return _user;
  }

  @Override
  public String execute() {

    UserIndexKey key = new UserIndexKey(_type, _id);
    IndexedUserDetails details = _userIndexService.getUserForIndexKey(key);
    if (details == null)
      return INPUT;
    
    UserIndex index = details.getUserIndex();
    User user = index.getUser();
    _user = _userService.getUserAsBean(user);
    
    return SUCCESS;
  }
}
