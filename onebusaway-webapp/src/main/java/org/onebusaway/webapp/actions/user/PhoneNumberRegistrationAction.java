package org.onebusaway.webapp.actions.user;

import org.onebusaway.webapp.actions.AbstractAction;

public class PhoneNumberRegistrationAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String _phoneNumber;

  private String _code;

  public void setPhoneNumber(String phoneNumber) {
    _phoneNumber = phoneNumber;
  }

  public String getCode() {
    return _code;
  }

  @Override
  public String execute() {

    if (_phoneNumber == null || _phoneNumber.length() == 0)
      return INPUT;

    _code = _currentUserService.registerPhoneNumber(_phoneNumber);

    return SUCCESS;
  }
}
