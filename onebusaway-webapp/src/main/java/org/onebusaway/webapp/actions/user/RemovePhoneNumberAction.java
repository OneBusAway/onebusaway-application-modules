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
package org.onebusaway.webapp.actions.user;

import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserIndexTypes;

public class RemovePhoneNumberAction extends AbstractRedirectAction {

  private static final long serialVersionUID = 1L;

  private String _phoneNumber;

  public void setPhoneNumber(String phoneNumber) {
    _phoneNumber = phoneNumber;
  }

  @Override
  public String execute() {

    if (_phoneNumber == null || _phoneNumber.length() == 0)
      return INPUT;

    UserIndexKey key = new UserIndexKey(UserIndexTypes.PHONE_NUMBER,_phoneNumber);
    _currentUserService.removeUserIndex(key);

    return SUCCESS;
  }
}
