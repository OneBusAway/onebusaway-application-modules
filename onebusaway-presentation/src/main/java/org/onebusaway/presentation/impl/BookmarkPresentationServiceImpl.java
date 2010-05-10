package org.onebusaway.presentation.impl;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.presentation.model.BookmarkWithStopsBean;
import org.onebusaway.presentation.services.BookmarkPresentationService;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.users.client.model.BookmarkBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookmarkPresentationServiceImpl implements
    BookmarkPresentationService {

  private TransitDataService _transitDataService;

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  @Override
  public List<BookmarkWithStopsBean> getBookmarksWithStops(
      List<BookmarkBean> bookmarks) {

    List<BookmarkWithStopsBean> beans = new ArrayList<BookmarkWithStopsBean>(
        bookmarks.size());

    for (BookmarkBean bookmark : bookmarks) {
      BookmarkWithStopsBean bean = new BookmarkWithStopsBean();
      bean.setId(bookmark.getId());
      bean.setName(bookmark.getName());
      bean.setStops(getStopsForStopIds(bookmark.getStopIds()));
      bean.setRouteFilter(bookmark.getRouteFilter());
      beans.add(bean);
    }

    return beans;
  }

  @Override
  public String getNameForStopIds(List<String> stopIds) {
    List<StopBean> stops = getStopsForStopIds(stopIds);
    return getNameForStops(stops);
  }

  @Override
  public String getNameForStops(List<StopBean> stops) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < stops.size(); i++) {
      if (i > 0) {
        if (i < stops.size() - 1)
          b.append(", ");
        else
          b.append(" and ");
      }
      StopBean stop = stops.get(i);
      b.append(stop.getName());
    }
    return b.toString();
  }

  /****
   * Private Methods
   ****/

  private List<StopBean> getStopsForStopIds(List<String> stopIds) {
    List<StopBean> stops = new ArrayList<StopBean>(stopIds.size());
    for (String stopId : stopIds) {
      StopBean stop = _transitDataService.getStop(stopId);
      if (stop != null)
        stops.add(stop);
    }
    return stops;
  }

  @Override
  public String getNameForBookmark(BookmarkWithStopsBean bookmark) {
    String name = bookmark.getName();
    if (name != null)
      return name;
    return getNameForStops(bookmark.getStops());
  }
}
