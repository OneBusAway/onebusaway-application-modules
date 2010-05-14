package org.onebusaway.webapp.actions;

public class LoginAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String _mode = "login";

  public void setMode(String mode) {
    _mode = mode;
  }

  public String getMode() {
    return _mode;
  }
}
