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
