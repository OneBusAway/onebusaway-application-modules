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
    "actionName", "bookmark-edit","parse","true","id","${bookmarkId}"})})
public class BookmarkCreateAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private String _name;

  private List<String> _stopIds;

  private Set<String> _routeIds = new HashSet<String>();

  private int _bookmarkId = -1;

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
    @Action(value = "/where/standard/bookmark-create"),
    @Action(value = "/where/iphone/bookmark-create"),
    @Action(value = "/where/text/bookmark-create")})
  public String execute() {

    if (_stopIds == null || _stopIds.isEmpty())
      return INPUT;
    
    RouteFilter routeFilter = new RouteFilter(_routeIds);
    _bookmarkId = _currentUserService.addStopBookmark(_name, _stopIds, routeFilter);

    return SUCCESS;
  }
}