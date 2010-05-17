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
