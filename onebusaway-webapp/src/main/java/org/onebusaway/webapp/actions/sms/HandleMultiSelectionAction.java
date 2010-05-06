package org.onebusaway.webapp.actions.sms;

public class HandleMultiSelectionAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private int _selectedIndex;

  public int getSelectedIndex() {
    return _selectedIndex;
  }

  @Override
  public String execute() {

    if (_text == null || _text.length() == 0)
      return INPUT;

    try {
      _selectedIndex = Integer.parseInt(_text) - 1;
    } catch (NumberFormatException ex) {
      return INPUT;
    }

    return getNextActionOrSuccess();
  }
}
