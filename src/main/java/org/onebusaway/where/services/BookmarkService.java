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
package org.onebusaway.where.services;

import org.onebusaway.gtfs.model.Stop;

import java.util.List;

public interface BookmarkService {

  public void addStopBookmark(String userId, String stopId)
      throws NoSuchStopException, BookmarksAtCapacityException,
      LocationAlreadyBookmarkedException;

  public List<Stop> getBookmarks(String userId);

  public Stop getLastSelection(String userId);

  public void setLastLocationByStop(String userId, String stopId)
      throws BookmarkException, NoSuchStopException;

  public void setLastLocationByStop(String userId, Stop stop)
      throws BookmarkException;

  /**
   * 
   * @param userId
   * @param index
   * @throws IndexOutOfBoundsException if the index is out of range of the
   *           bookmark list
   */
  public void deleteBookmarkByIndex(String userId, int index)
      throws IndexOutOfBoundsException;
}
