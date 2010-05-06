package org.onebusaway.webapp.actions.sms;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

@Results( {
    @Result(type = "chain", name = "command-help", location = "command-help"),
    @Result(type = "chain", name = "command-bookmark", location = "command-bookmark"),
    @Result(type = "chain", name = "command-bookmarks", location = "command-bookmarks"),
    @Result(type = "chain", name = "command-setSearchLocation", location = "command-set-search-location"),
    @Result(type = "chain", name = "command-reset", location = "command-reset")})
public class CommandAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private String _command;

  private String _arg;

  public String getCommand() {
    return _command;
  }

  public String getArg() {
    return _arg;
  }

  @Override
  public String execute() {

    if (_text == null || _text.length() == 0 || !_text.startsWith("#"))
      return INPUT;

    _command = _text.substring(1);

    int index = _command.indexOf(' ');

    if (index != -1) {
      _arg = _command.substring(index + 1);
      _command = _command.substring(0, index);
    }

    if (_arg != null)
      _arg = _arg.trim();

    _command = Commands.getClosestCommand(_command);

    if (_command == null)
      return INPUT;

    return "command-" + _command;
  }
}
