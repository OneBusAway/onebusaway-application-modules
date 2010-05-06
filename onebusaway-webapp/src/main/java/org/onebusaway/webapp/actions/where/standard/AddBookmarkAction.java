package org.onebusaway.webapp.actions.where.standard;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.webapp.actions.AbstractAction;

@Results( {@Result(type = "redirectAction", params = {
    "actionName", "bookmarks"})})
public class AddBookmarkAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String _name;

  private List<String> _stopIds = new ArrayList<String>();

  public void setName(String name) {
    _name = name;
  }

  public void setStopId(List<String> stopIds) {
    _stopIds = stopIds;
  }

  @Override
  public String execute() {

    if (_stopIds.isEmpty())
      return INPUT;

    _currentUserService.addStopBookmark(_name, _stopIds, new RouteFilter());

    return SUCCESS;
  }
}