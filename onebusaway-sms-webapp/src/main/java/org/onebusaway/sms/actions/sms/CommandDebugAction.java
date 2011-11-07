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
package org.onebusaway.sms.actions.sms;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.services.BookmarkException;

import com.opensymphony.xwork2.ModelDriven;

public class CommandDebugAction extends AbstractTextmarksAction implements
    ModelDriven<UserBean> {

  private static final long serialVersionUID = 1L;

  private UserBean _user;

  public UserBean getModel() {
    return _user;
  }

  @Override
  public String execute() throws ServiceException, BookmarkException {

    _user = _currentUserService.getCurrentUser();

    return SUCCESS;
  }
}
