/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.sms.actions.sms;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

@Results( {
    @Result(type = "chain", name = "command-help", location = "command-help"),
    @Result(type = "chain", name = "command-bookmark", location = "command-bookmark"),
    @Result(type = "chain", name = "command-bookmarks", location = "command-bookmarks"),
    @Result(type = "chain", name = "command-setSearchLocation", location = "command-set-search-location"),
    @Result(type = "chain", name = "command-register", location = "command-register"),
    @Result(type = "chain", name = "command-reset", location = "command-reset"),
    @Result(type = "chain", name = "command-debug", location = "command-debug")
    })
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
