package org.onebusaway.sms.actions;

public class CommandSetSearchLocationAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  @Override
  public String execute() {
    pushNextAction("default-search-location-set");
    return "query-default-search-location";
  }
}
