package org.onebusaway.webapp.actions.where;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.webapp.actions.AbstractAction;

@Results( {@Result(type = "redirectAction", params = {"actionName", "bookmarks"})})
public class BookmarkDeleteAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private int _id;

  public void setId(int id) {
    _id = id;
  }

  @Override
  public String execute() {
    _currentUserService.deleteStopBookmarks(_id);
    return SUCCESS;
  }
}