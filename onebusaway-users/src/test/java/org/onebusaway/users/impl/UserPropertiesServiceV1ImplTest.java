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
package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.services.BookmarkException;
import org.onebusaway.users.services.UserDao;

public class UserPropertiesServiceV1ImplTest {

  private UserPropertiesServiceV1Impl _service;

  private UserDao _dao;

  @Before
  public void setup() throws IOException {

    _service = new UserPropertiesServiceV1Impl();

    _dao = Mockito.mock(UserDao.class);
    _service.setUserDao(_dao);

    _service.setUserPropertiesMigration(new UserPropertiesMigrationImpl());
  }

  @Test
  public void testAddStopBookmark() throws BookmarkException {

    User user = createUser();

    _service.addStopBookmark(user, "bookmkark", Arrays.asList("1"),
        new RouteFilter());

    List<String> bookmarks = getProperties(user).getBookmarkedStopIds();
    assertEquals(1, bookmarks.size());
    assertTrue(bookmarks.contains("1"));
  }

  @Test
  public void testMergeUsers01() throws BookmarkException {

    User userA = createUser();

    _service.setLastSelectedStopIds(userA, Arrays.asList("A"));
    _service.addStopBookmark(userA, "bookmark a", Arrays.asList("A", "C"),
        new RouteFilter());

    User userB = createUser();

    _service.setDefaultLocation(userB, "here", 47.0, -122.0);
    _service.addStopBookmark(userB, "bookmark b", Arrays.asList("C", "B"),
        new RouteFilter());
    _service.mergeProperties(userA, userB);

    UserPropertiesV1 props = getProperties(userB);
    List<String> bookmarks = props.getBookmarkedStopIds();
    assertEquals(4, bookmarks.size());
    assertEquals("C", bookmarks.get(0));
    assertEquals("B", bookmarks.get(1));
    assertEquals("A", bookmarks.get(2));
    assertEquals("C", bookmarks.get(3));

    assertEquals("here", props.getDefaultLocationName());
    assertEquals(47.0, props.getDefaultLocationLat(), 0.0);
    assertEquals(-122.0, props.getDefaultLocationLon(), 0.0);

    assertEquals("A", props.getLastSelectedStopId());
  }

  @Test
  public void testMergeUsers02() throws BookmarkException {

    User userA = createUser();

    _service.setLastSelectedStopIds(userA, Arrays.asList("A"));
    _service.setDefaultLocation(userA, "here", 47.0, -122.0);
    _service.addStopBookmark(userA, "bookmark A", Arrays.asList("A", "C"),
        new RouteFilter());

    User userB = createUser();

    _service.mergeProperties(userA, userB);

    UserPropertiesV1 props = getProperties(userB);
    List<String> bookmarks = props.getBookmarkedStopIds();
    assertEquals(2, bookmarks.size());
    assertEquals("A", bookmarks.get(0));
    assertEquals("C", bookmarks.get(1));

    assertEquals("here", props.getDefaultLocationName());
    assertEquals(47.0, props.getDefaultLocationLat(), 0.0);
    assertEquals(-122.0, props.getDefaultLocationLon(), 0.0);

    assertEquals("A", props.getLastSelectedStopId());
  }

  @Test
  public void testMergeUsers03() throws BookmarkException {

    User userA = createUser();

    _service.setLastSelectedStopIds(userA, Arrays.asList("A"));
    _service.setDefaultLocation(userA, "here", 47.0, -122.0);
    _service.addStopBookmark(userA, "bookmark a", Arrays.asList("A", "C"),
        new RouteFilter());

    User userB = createUser();

    _service.setLastSelectedStopIds(userB, Arrays.asList("B"));
    _service.setDefaultLocation(userB, "there", 48.0, -123.0);
    _service.addStopBookmark(userB, "bookmark b", Arrays.asList("B", "A"),
        new RouteFilter());

    _service.mergeProperties(userA, userB);

    UserPropertiesV1 props = getProperties(userB);
    List<String> bookmarks = props.getBookmarkedStopIds();
    assertEquals(4, bookmarks.size());
    assertEquals("B", bookmarks.get(0));
    assertEquals("A", bookmarks.get(1));
    assertEquals("A", bookmarks.get(2));
    assertEquals("C", bookmarks.get(3));

    assertEquals("there", props.getDefaultLocationName());
    assertEquals(48.0, props.getDefaultLocationLat(), 0.0);
    assertEquals(-123.0, props.getDefaultLocationLon(), 0.0);

    assertEquals("B", props.getLastSelectedStopId());
  }

  private User createUser() {
    User user = new User();
    user.setProperties(new UserPropertiesV1());
    return user;
  }

  private UserPropertiesV1 getProperties(User user) {
    return (UserPropertiesV1) user.getProperties();
  }
}
