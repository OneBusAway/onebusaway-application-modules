package org.onebusaway.webapp.actions.where;

public class QueryDefaultSearchLocationAction extends AbstractWhereAction {

  private static final long serialVersionUID = 1L;

  @Override
  public String execute() {
    pushNextAction("set-default-search-location");
    return SUCCESS;
  }
}
