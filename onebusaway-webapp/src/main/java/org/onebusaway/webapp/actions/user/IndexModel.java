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