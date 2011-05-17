package org.onebusaway.webapp.actions;


public class LoginAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private boolean _failure;

  private String _mode = "login";

  public void setFailure(boolean failure) {
    _failure = failure;
  }

  public boolean isFailure() {
    return _failure;
  }

  public void setMode(String mode) {
    _mode = mode;
  }

  public String getMode() {
    return _mode;
  }
}
