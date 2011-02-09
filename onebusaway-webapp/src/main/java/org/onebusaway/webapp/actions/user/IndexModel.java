/**
 * 
 */
package org.onebusaway.webapp.actions.user;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.client.model.UserIndexBean;

public class IndexModel {

  private UserBean user;

  private List<UserIndexBean> phoneIndices = new ArrayList<UserIndexBean>();

  public UserBean getUser() {
    return user;
  }

  public void setUser(UserBean user) {
    this.user = user;
  }

  public List<UserIndexBean> getPhoneIndices() {
    return phoneIndices;
  }

  public void setPhoneIndices(List<UserIndexBean> phoneIndices) {
    this.phoneIndices = phoneIndices;
  }
}