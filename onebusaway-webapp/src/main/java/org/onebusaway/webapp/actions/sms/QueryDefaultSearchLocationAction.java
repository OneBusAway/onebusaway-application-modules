package org.onebusaway.webapp.actions.sms;

public class QueryDefaultSearchLocationAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  @Override
  public String execute() {
    pushNextAction("set-default-search-location");
    return SUCCESS;
  }
}
