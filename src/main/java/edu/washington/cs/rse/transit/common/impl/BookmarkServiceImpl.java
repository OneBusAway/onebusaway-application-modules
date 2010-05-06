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
package edu.washington.cs.rse.transit.common.impl;

import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.LocationBookmarks;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.services.BookmarkException;
import edu.washington.cs.rse.transit.common.services.BookmarkService;
import edu.washington.cs.rse.transit.common.services.BookmarksAtCapacityException;
import edu.washington.cs.rse.transit.common.services.LocationAlreadyBookmarkedException;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookmarkServiceImpl implements BookmarkService {

  private MetroKCDAO _dao;

  @Autowired
  public void setMetroKCDAO(MetroKCDAO dao) {
    _dao = dao;
  }

  public void addStopBookmark(String userId, int stopId)
      throws NoSuchStopException, BookmarksAtCapacityException,
      LocationAlreadyBookmarkedException {

    StopLocation stop = _dao.getStopLocationById(stopId);
    if (stop == null)
      throw new NoSuchStopException();
    addLocationBookmark(userId, stop);
  }

  public List<StopLocation> getBookmarks(String userId) {
    LocationBookmarks bookmarks = _dao.getBookmarksByUserId(userId);
    return bookmarks.getBookmarks();
  }

  public StopLocation getLastSelection(String userId) {
    LocationBookmarks bookmarks = _dao.getBookmarksByUserId(userId);
    return bookmarks.getLastSelection();
  }

  public void setLastLocationByStop(String userId, int stopId)
      throws BookmarkException, NoSuchStopException {
    StopLocation stop = _dao.getStopLocationById(stopId);
    if (stop == null)
      throw new NoSuchStopException();
    setLastLocationByStop(userId, stop);
  }

  public void setLastLocationByStop(String userId, StopLocation stop)
      throws BookmarkException {
    LocationBookmarks lbs = _dao.getBookmarksByUserId(userId);
    lbs.setLastSelection(stop);
    _dao.update(lbs);
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

    LocationBookmarks lbs = _dao.getBookmarksByUserId(userId);
    List<StopLocation> bookmarks = lbs.getBookmarks();

    bookmarks.remove(index);

    _dao.saveOrUpdate(lbs);
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private void addLocationBookmark(String userId, StopLocation toAdd)
      throws BookmarksAtCapacityException, LocationAlreadyBookmarkedException {

    if (userId == null)
      throw new IllegalArgumentException("userId cannot be null");

    if (toAdd == null)
      throw new IllegalArgumentException("stop cannot be null");

    LocationBookmarks lbs = _dao.getBookmarksByUserId(userId);
    List<StopLocation> bookmarks = lbs.getBookmarks();

    if (bookmarks.size() == 9)
      throw new BookmarksAtCapacityException();

    for (StopLocation bookmark : bookmarks) {
      if (bookmark.equals(toAdd))
        throw new LocationAlreadyBookmarkedException();
    }

    bookmarks.add(toAdd);
    _dao.saveOrUpdate(lbs);
  }
}
