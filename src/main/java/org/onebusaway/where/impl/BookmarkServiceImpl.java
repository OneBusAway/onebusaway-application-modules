/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.impl;

import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.LocationBookmarks;
import org.onebusaway.where.services.BookmarkException;
import org.onebusaway.where.services.BookmarkService;
import org.onebusaway.where.services.BookmarksAtCapacityException;
import org.onebusaway.where.services.LocationAlreadyBookmarkedException;
import org.onebusaway.where.services.NoSuchStopException;
import org.onebusaway.where.services.WhereDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookmarkServiceImpl implements BookmarkService {

  @Autowired
  private GtfsDao _gtfsDao;

  @Autowired
  private WhereDao _whereDao;

  public void addStopBookmark(String userId, String stopId)
      throws NoSuchStopException, BookmarksAtCapacityException,
      LocationAlreadyBookmarkedException {

    Stop stop = _gtfsDao.getStopById(stopId);
    if (stop == null)
      throw new NoSuchStopException();
    addLocationBookmark(userId, stop);
  }

  public List<Stop> getBookmarks(String userId) {
    LocationBookmarks bookmarks = _whereDao.getBookmarksByUserId(userId);
    return bookmarks.getBookmarks();
  }

  public Stop getLastSelection(String userId) {
    LocationBookmarks bookmarks = _whereDao.getBookmarksByUserId(userId);
    return bookmarks.getLastSelection();
  }

  public void setLastLocationByStop(String userId, String stopId)
      throws BookmarkException, NoSuchStopException {
    Stop stop = _gtfsDao.getStopById(stopId);
    if (stop == null)
      throw new NoSuchStopException();
    setLastLocationByStop(userId, stop);
  }

  public void setLastLocationByStop(String userId, Stop stop)
      throws BookmarkException {
    LocationBookmarks lbs = _whereDao.getBookmarksByUserId(userId);
    lbs.setLastSelection(stop);
    _whereDao.update(lbs);
  }

  /**
   * 
   * @param userId
   * @param index
   * @throws IndexOutOfBoundsException if the index is out of range of the
   *           bookmark list
   */
  public void deleteBookmarkByIndex(String userId, int index)
      throws IndexOutOfBoundsException {

    LocationBookmarks lbs = _whereDao.getBookmarksByUserId(userId);
    List<Stop> bookmarks = lbs.getBookmarks();

    bookmarks.remove(index);

    _whereDao.update(lbs);
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private void addLocationBookmark(String userId, Stop toAdd)
      throws BookmarksAtCapacityException, LocationAlreadyBookmarkedException {

    if (userId == null)
      throw new IllegalArgumentException("userId cannot be null");

    if (toAdd == null)
      throw new IllegalArgumentException("stop cannot be null");

    LocationBookmarks lbs = _whereDao.getBookmarksByUserId(userId);
    List<Stop> bookmarks = lbs.getBookmarks();

    if (bookmarks.size() == 9)
      throw new BookmarksAtCapacityException();

    for (Stop bookmark : bookmarks) {
      if (bookmark.equals(toAdd))
        throw new LocationAlreadyBookmarkedException();
    }

    bookmarks.add(toAdd);
    _whereDao.update(lbs);
  }
}
