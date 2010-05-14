package org.onebusaway.sms.actions;

public class CommandHelpAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private String _command;

  private String _arg;

  public String getCommand() {
    return _command;
  }

  public void setArg(String arg) {
    _arg = arg;
  }

  @Override
  public String execute() {

    if (_arg == null)
      return SUCCESS;

    if (_arg.startsWith("#"))
      _arg = _arg.substring(1);
    
    _arg = Commands.getCanonicalCommand(_arg);

    return _arg;
  }
}
