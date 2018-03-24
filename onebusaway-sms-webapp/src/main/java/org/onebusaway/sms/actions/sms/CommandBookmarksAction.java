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
package org.onebusaway.sms.actions.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.presentation.services.BookmarkPresentationService;
import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.services.BookmarkException;
import org.springframework.beans.factory.annotation.Autowired;

public class CommandBookmarksAction extends AbstractTextmarksAction {

  private static final long serialVersionUID = 1L;

  private BookmarkPresentationService _bookmarkPresentationService;

  private String _arg;

  private List<BookmarkWithStopsBean> _bookmarks = new ArrayList<BookmarkWithStopsBean>();

  /**
   * Used when chaining to "arrivals-and-departures"
   */
  private List<String> _stopIds;

  /**
   * Used when chaining to "arrivals-and-departures"
   */
  private Set<String> _routeFilter;

  @Autowired
  public void setBookmarkPresentationService(
      BookmarkPresentationService bookmarkPresentationService) {
    _bookmarkPresentationService = bookmarkPresentationService;
  }

  public List<BookmarkWithStopsBean> getBookmarks() {
    return _bookmarks;
  }

  public void setArg(String arg) {
    _arg = arg;
  }

  public List<String> getStopIds() {
    return _stopIds;
  }

  public Set<String> getRouteFilter() {
    return _routeFilter;
  }

  @Override
  public String execute() throws ServiceException, BookmarkException {

    UserBean currentUser = _currentUserService.getCurrentUser();

    if (_arg != null && _arg.length() > 0) {

      if (_arg.startsWith("add")) {
        
        if( currentUser == null)
          return "noUser";
        
        List<String> lastSelectedStopIds = currentUser.getLastSelectedStopIds();
        if (!lastSelectedStopIds.isEmpty()) {
          String name = _bookmarkPresentationService.getNameForStopIds(lastSelectedStopIds);
          _currentUserService.addStopBookmark(name, lastSelectedStopIds,
              new RouteFilter());
        }

        return "added";
      }

      if (_arg.startsWith("delete")) {
        int index = _arg.indexOf(' ');
        if (index == -1)
          return INPUT;
        int bookmarkIndex = Integer.parseInt(_arg.substring(index + 1).trim()) - 1;
        _currentUserService.deleteStopBookmarks(bookmarkIndex);
        return "deleted";
      }

      if (_arg.matches("\\d+")) {
        int index = Integer.parseInt(_arg) - 1;

        List<BookmarkBean> bookmarks = currentUser.getBookmarks();
        if (index < 0 || index >= bookmarks.size())
          return INPUT;

        BookmarkBean bookmark = bookmarks.get(index);
        _stopIds = bookmark.getStopIds();
        _routeFilter = bookmark.getRouteFilter().getRouteIds();
        
        return "arrivals-and-departures";
      }
    }

    _bookmarks = _bookmarkPresentationService.getBookmarksWithStops(currentUser.getBookmarks());

    return SUCCESS;
  }

  public String getBookmarkName(BookmarkWithStopsBean bookmark) {
    return _bookmarkPresentationService.getNameForBookmark(bookmark);
  }
}
