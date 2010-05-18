package org.onebusaway.webapp.actions.where;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.webapp.actions.AbstractAction;

@Results( {@Result(type = "redirectAction", params = {
    "actionName", "bookmarks"})})
public class BookmarkUpdateAction extends AbstractAction {

  private static final long serialVersionUID = 1L;
  
  private int _id;

  private String _name;

  private List<String> _stopIds;

  private Set<String> _routeIds = new HashSet<String>();

  private int _bookmarkId = -1;
  
  public void setId(int id){
    _id = id;
  }

  public void setName(String name) {
    _name = name;
  }

  public void setStopId(List<String> stopIds) {
    _stopIds = stopIds;
  }
  
  public void setRouteId(Set<String> routeIds) {
    _routeIds = routeIds;
  }
  
  public int getBookmarkId() {
    return _bookmarkId;
  }

  @Override
  @Actions( {
    @Action(value = "/where/standard/bookmark-update"),
    @Action(value = "/where/iphone/bookmark-update"),
    @Action(value = "/where/text/bookmark-update")})
  public String execute() {

    
    if (_stopIds == null || _stopIds.isEmpty())
      return INPUT;
    
    RouteFilter routeFilter = new RouteFilter(_routeIds);
    _currentUserService.updateStopBookmark(_id,_name,_stopIds,routeFilter);

    return SUCCESS;
  }
}