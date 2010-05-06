package org.onebusaway.webapp.gwt.mobile_application.control;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.mobile_application.model.Bookmark;

public class MobileApplicationDao {
  
  private int _bookmarkIndex = 0;

  private List<Bookmark> _bookmarks = new ArrayList<Bookmark>();
  
  private List<StopBean> _recentStops = new ArrayList<StopBean>();
  
  
  public List<Bookmark> getBookmarks() {
    return _bookmarks;
  }
  
  public void addBookmark(Bookmark bookmark) {
    bookmark.setId(_bookmarkIndex++);
    _bookmarks.add(bookmark);
    
  }
  
  public void clearBookmarks() {
    _bookmarks.clear();
  }
  
  public List<StopBean> getRecentStops() {
    return _recentStops;
  }

  public void addRecentStop(StopBean stop) {
    _recentStops.remove(stop);
    _recentStops.add(0, stop);
    while (_recentStops.size() > 10)
      _recentStops.remove(_recentStops.size() - 1);
  }

  public void clearRecentStops() {
    _recentStops.clear();
  }
}
