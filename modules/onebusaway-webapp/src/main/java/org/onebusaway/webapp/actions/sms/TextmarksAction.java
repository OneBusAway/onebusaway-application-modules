package org.onebusaway.webapp.actions.sms;

import org.onebusaway.exceptions.ServiceException;

public class TextmarksAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  @Override
  public String execute() throws ServiceException {

    String nextAction = getNextActionOrSuccess();

    if (nextAction.equals(SUCCESS))
      return "arrivals-and-departures";

    return nextAction;
  }
}
