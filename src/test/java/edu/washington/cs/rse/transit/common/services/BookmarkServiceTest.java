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
package edu.washington.cs.rse.transit.common.services;

import edu.washington.cs.rse.transit.BaseTest;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.services.BookmarkException;
import edu.washington.cs.rse.transit.common.services.BookmarkService;
import edu.washington.cs.rse.transit.common.services.BookmarksAtCapacityException;
import edu.washington.cs.rse.transit.common.services.LocationAlreadyBookmarkedException;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;

import com.carbonfive.testutils.spring.dbunit.DataSet;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@DataSet
public class BookmarkServiceTest extends BaseTest {

  private BookmarkService _service;

  @Autowired
  public void setBookmarkService(BookmarkService service) {
    _service = service;
  }

  /*****************************************************************************
   * Test Methods
   ****************************************************************************/

  public void testAddAndRemove() throws BookmarkException, NoSuchStopException {

    _service.addStopBookmark("1234", 10020);

    List<StopLocation> bookmarks = _service.getBookmarks("1234");
    assertEquals(1, bookmarks.size());

    StopLocation stop = bookmarks.get(0);
    assertEquals(10020, stop.getId());

    _service.deleteBookmarkByIndex("1234", 0);

    bookmarks = _service.getBookmarks("1234");
    assertEquals(0, bookmarks.size());
  }

  public void testAddDuplicate() throws BookmarkException, NoSuchStopException {

    _service.addStopBookmark("1234", 10020);
    try {
      _service.addStopBookmark("1234", 10020);
      fail();
    } catch (LocationAlreadyBookmarkedException ex) {

    }

    List<StopLocation> bookmarks = _service.getBookmarks("1234");
    assertEquals(1, bookmarks.size());

    StopLocation stop = bookmarks.get(0);
    assertEquals(10020, stop.getId());
  }

  public void testAddNonExistingStop() throws BookmarkException {
    try {
      _service.addStopBookmark("1234", -1);
    } catch (NoSuchStopException ex) {

    }
  }

  public void testAddTooManyBookmarks() throws BookmarkException,
      NoSuchStopException {
    for (int i = 0; i < 9; i++) {
      int id = 10000 + i * 10;
      _service.addStopBookmark("1234", id);
    }

    try {
      _service.addStopBookmark("1234", 10090);
      fail();
    } catch (BookmarksAtCapacityException ex) {

    }
  }

  public void deleteBookmarks() throws BookmarkException, NoSuchStopException {

    _service.addStopBookmark("1234", 10000);
    _service.addStopBookmark("1234", 10010);
    _service.addStopBookmark("1234", 10020);
    _service.addStopBookmark("1234", 10030);
    _service.addStopBookmark("1234", 10040);

    try {
      _service.deleteBookmarkByIndex("1234", -1);
      fail();
    } catch (IndexOutOfBoundsException ex) {

    }

    try {
      _service.deleteBookmarkByIndex("1234", 5);
      fail();
    } catch (IndexOutOfBoundsException ex) {

    }

    _service.deleteBookmarkByIndex("1234", 2);

    List<StopLocation> marks = _service.getBookmarks("1234");
    assertEquals(4, marks.size());

    assertEquals(10000, marks.get(0).getId());
    assertEquals(10010, marks.get(1).getId());
    assertEquals(10030, marks.get(2).getId());
    assertEquals(10040, marks.get(3).getId());

    _service.deleteBookmarkByIndex("1234", 3);

    marks = _service.getBookmarks("1234");
    assertEquals(3, marks.size());

    assertEquals(10000, marks.get(0).getId());
    assertEquals(10010, marks.get(1).getId());
    assertEquals(10030, marks.get(2).getId());

    _service.deleteBookmarkByIndex("1234", 0);

    marks = _service.getBookmarks("1234");
    assertEquals(2, marks.size());

    assertEquals(10010, marks.get(0).getId());
    assertEquals(10030, marks.get(1).getId());
  }

  public void testAddDuplicateBetweenTwoUsers() throws BookmarkException,
      NoSuchStopException {

    _service.addStopBookmark("1234", 10020);
    _service.addStopBookmark("5678", 10030);
    _service.addStopBookmark("1234", 10030);
    _service.addStopBookmark("5678", 10020);

    List<StopLocation> bookmarksA = _service.getBookmarks("1234");
    assertEquals(2, bookmarksA.size());
    assertEquals(10020, bookmarksA.get(0).getId());
    assertEquals(10030, bookmarksA.get(1).getId());

    List<StopLocation> bookmarksB = _service.getBookmarks("5678");
    assertEquals(2, bookmarksB.size());
    assertEquals(10030, bookmarksB.get(0).getId());
    assertEquals(10020, bookmarksB.get(1).getId());

    _service.deleteBookmarkByIndex("1234", 0);
    _service.deleteBookmarkByIndex("5678", 1);

    bookmarksA = _service.getBookmarks("1234");
    assertEquals(1, bookmarksA.size());
    assertEquals(10030, bookmarksA.get(0).getId());

    bookmarksB = _service.getBookmarks("5678");
    assertEquals(1, bookmarksB.size());
    assertEquals(10030, bookmarksB.get(0).getId());
  }
}
