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

import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.presentation.services.BookmarkPresentationService;
import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.webapp.actions.AbstractAction;
import org.springframework.beans.factory.annotation.Autowired;

public class BookmarksAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private BookmarkPresentationService _bookmarkPresentationService;

  private List<BookmarkWithStopsBean> _bookmarks;

  @Autowired
  public void setBookmarkPresentationService(
      BookmarkPresentationService bookmarkPresentationService) {
    _bookmarkPresentationService = bookmarkPresentationService;
  }

  public List<BookmarkWithStopsBean> getBookmarks() {
    return _bookmarks;
  }

  @Override
  @Actions( {
      @Action(value = "/where/standard/bookmarks"),
      @Action(value = "/where/iphone/bookmarks"),
      @Action(value = "/where/text/bookmarks")})
  public String execute() {
    UserBean user = getCurrentUser();
    List<BookmarkBean> bookmarks = user.getBookmarks();
    _bookmarks = _bookmarkPresentationService.getBookmarksWithStops(bookmarks);
    return SUCCESS;
  }

  public String getBookmarkName(BookmarkWithStopsBean bookmark) {
    return _bookmarkPresentationService.getNameForBookmark(bookmark);
  }

}
