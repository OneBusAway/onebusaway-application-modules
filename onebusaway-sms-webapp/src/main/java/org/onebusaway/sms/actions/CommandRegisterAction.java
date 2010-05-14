package org.onebusaway.sms.actions;

public class CommandRegisterAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private String _arg;

  public void setArg(String arg) {
    _arg = arg;
  }

  @Override
  public String execute() {

    if (_arg == null || _arg.length() == 0)
      return INPUT;

    if( _currentUserService.completePhoneNumberRegistration(_arg) )
      return SUCCESS;
    
    return "invalidRegistrationCode";
  }
}
