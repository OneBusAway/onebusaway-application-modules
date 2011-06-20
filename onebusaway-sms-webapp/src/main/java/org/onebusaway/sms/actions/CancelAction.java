package org.onebusaway.sms.actions;

public class CancelAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private int _selectedIndex;

  public int getSelectedIndex() {
    return _selectedIndex;
  }

  @Override
  public String execute() {
    clearNextActions();
    return SUCCESS;
  }
}
