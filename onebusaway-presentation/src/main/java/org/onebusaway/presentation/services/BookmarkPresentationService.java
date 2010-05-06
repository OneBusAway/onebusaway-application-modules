package org.onebusaway.presentation.services;

import java.util.List;

import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.users.client.model.BookmarkBean;

public interface BookmarkPresentationService {
  
  public List<BookmarkWithStopsBean> getBookmarksWithStops(
      List<BookmarkBean> bookmarks);

  public String getNameForStops(List<StopBean> stops);

  public String getNameForStopIds(List<String> stopIds);

  public String getNameForBookmark(BookmarkWithStopsBean bookmark);
}
