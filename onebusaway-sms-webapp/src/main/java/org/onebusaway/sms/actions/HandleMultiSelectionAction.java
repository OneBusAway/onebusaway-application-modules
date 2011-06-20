package org.onebusaway.sms.actions;

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

    if (_text.startsWith("#")) {
      clearNextActions();
      return "command";
    }

    try {
      _selectedIndex = Integer.parseInt(_text) - 1;
    } catch (NumberFormatException ex) {
      return INPUT;
    }

    if (_selectedIndex == -1)
      return "cancel";

    return getNextActionOrSuccess();
  }
}
