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

import java.util.ArrayList;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.RouteFilterBean;
import org.onebusaway.users.client.model.UserBean;

@Results({@Result(location = "stop.jspx")})
public class BookmarkAction extends StopAction {

  private static final long serialVersionUID = 1L;

  @Override
  @Actions({
      @Action(value = "/where/standard/bookmark"),
      @Action(value = "/where/iphone/bookmark"),
      @Action(value = "/where/text/bookmark")})
  public String execute() {

    BookmarkBean bookmark = getBookmark();
    if (bookmark == null)
      return INPUT;

    /**
     * We need to replace the bookmark ids in the "id" parameter with the stop
     * ids
     */
    _ids = bookmark.getStopIds();
    _model.setStopIds(bookmark.getStopIds());

    RouteFilterBean filter = bookmark.getRouteFilter();
    if (filter != null) {
      super.setRoute(new ArrayList<String>(filter.getRouteIds()));
    }

    setTitle(bookmark.getName());

    return super.execute();
  }

  private BookmarkBean getBookmark() {
    if (CollectionsLibrary.isEmpty(_ids))
      return null;
    int id = Integer.parseInt(_ids.get(0));
    UserBean user = getCurrentUser();
    for (BookmarkBean bookmark : user.getBookmarks()) {
      if (bookmark.getId() == id)
        return bookmark;
    }
    return null;
  }

}