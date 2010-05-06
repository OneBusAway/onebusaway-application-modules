package org.onebusaway.webapp.actions.sms;

import org.onebusaway.exceptions.ServiceException;

public class TextmarksAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  @Override
  public String execute() throws ServiceException {
    
    // Short circuit to command if we have a reset command
    if( _text != null && _text.startsWith("#reset"))
      return "command";

    String nextAction = getNextActionOrSuccess();

    if (nextAction.equals(SUCCESS)) {
      if( _text != null && _text.startsWith("#"))
        return "command";
      return "stop-by-number";
    }

    return nextAction;
  }
}
