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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.properties.Bookmark;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.model.properties.UserPropertiesV2;
import org.onebusaway.users.model.properties.UserPropertiesV3;

public class UserPropertiesMigrationImplTest {

  private UserPropertiesMigrationImpl _service;

  @Before
  public void setup() {
    _service = new UserPropertiesMigrationImpl();
  }

  @Test
  public void testNeedsMigration() {

    UserPropertiesV1 v1 = new UserPropertiesV1();
    assertFalse(_service.needsMigration(v1, UserPropertiesV1.class));
    assertTrue(_service.needsMigration(v1, UserPropertiesV2.class));

    UserPropertiesV2 v2 = new UserPropertiesV2();
    assertTrue(_service.needsMigration(v2, UserPropertiesV1.class));
    assertFalse(_service.needsMigration(v2, UserPropertiesV2.class));
  }

  @Test
  public void testV1ToV2Migration() {

    UserPropertiesV1 v1 = new UserPropertiesV1();
    v1.setDefaultLocationLat(47.0);
    v1.setDefaultLocationLon(-122.0);
    v1.setDefaultLocationName("Seattle");
    v1.setLastSelectedStopId("1_29214");
    v1.setRememberPreferencesEnabled(true);
    v1.setBookmarkedStopIds(Arrays.asList("1_29214", "1_75403"));

    UserPropertiesV1 result = _service.migrate(v1, UserPropertiesV1.class);
    assertTrue(v1 == result);

    UserPropertiesV2 v2 = _service.migrate(v1, UserPropertiesV2.class);

    assertTrue(v2.isRememberPreferencesEnabled());

    assertEquals(47.0, v2.getDefaultLocationLat(), 0.0);
    assertEquals(-122.0, v2.getDefaultLocationLon(), 0.0);
    assertEquals("Seattle", v2.getDefaultLocationName());

    List<Bookmark> bookmarks = v2.getBookmarks();
    assertEquals(2, bookmarks.size());

    Bookmark bookmark = bookmarks.get(0);
    assertEquals(0,bookmark.getId());
    assertNull(bookmark.getName());
    assertEquals(Arrays.asList("1_29214"), bookmark.getStopIds());
    assertTrue(bookmark.getRouteFilter().getRouteIds().isEmpty());

    bookmark = bookmarks.get(1);
    assertEquals(1,bookmark.getId());
    assertNull(bookmark.getName());
    assertEquals(Arrays.asList("1_75403"), bookmark.getStopIds());
    assertTrue(bookmark.getRouteFilter().getRouteIds().isEmpty());
  }

  @Test
  public void testV2ToV1Migration() {

    UserPropertiesV2 v2 = new UserPropertiesV2();
    v2.setDefaultLocationLat(47.0);
    v2.setDefaultLocationLon(-122.0);
    v2.setDefaultLocationName("Seattle");
    v2.setRememberPreferencesEnabled(true);

    Bookmark b1 = new Bookmark(0,null,Arrays.asList("1_29214"),new RouteFilter());
    Bookmark b2 = new Bookmark(1,null,Arrays.asList("1_75403", "1_75414"), new RouteFilter());
    v2.setBookmarks(Arrays.asList(b1, b2));

    UserPropertiesV2 result = _service.migrate(v2, UserPropertiesV2.class);
    assertTrue(v2 == result);

    UserPropertiesV1 v1 = _service.migrate(v2, UserPropertiesV1.class);

    assertTrue(v1.isRememberPreferencesEnabled());

    assertNull(v1.getLastSelectedStopId());

    assertEquals(47.0, v1.getDefaultLocationLat(), 0.0);
    assertEquals(-122.0, v1.getDefaultLocationLon(), 0.0);
    assertEquals("Seattle", v1.getDefaultLocationName());

    assertEquals(Arrays.asList("1_29214", "1_75403", "1_75414"),
        v1.getBookmarkedStopIds());
  }
  @Test
  public void testV2ToV3Migration() {

    UserPropertiesV2 v2 = new UserPropertiesV2();
    v2.setDefaultLocationLat(47.0);
    v2.setDefaultLocationLon(-122.0);
    v2.setDefaultLocationName("Seattle");
    v2.setRememberPreferencesEnabled(true);

    Bookmark b1 = new Bookmark(0,null,Arrays.asList("1_29214"),new RouteFilter());
    Bookmark b2 = new Bookmark(1,null,Arrays.asList("1_75403", "1_75414"), new RouteFilter());
    v2.setBookmarks(Arrays.asList(b1, b2));

    UserPropertiesV2 result = _service.migrate(v2, UserPropertiesV2.class);
    assertTrue(v2 == result);

    UserPropertiesV3 v3 = _service.migrate(v2, UserPropertiesV3.class);

    assertTrue(v3.isRememberPreferencesEnabled());

    assertEquals(47.0, v3.getDefaultLocationLat(), 0.0);
    assertEquals(-122.0, v3.getDefaultLocationLon(), 0.0);
    assertEquals("Seattle", v3.getDefaultLocationName());

    List<Bookmark> bookmarks = v3.getBookmarks();
    assertEquals(2, bookmarks.size());

    Bookmark bookmark = bookmarks.get(0);
    assertEquals(0,bookmark.getId());
    assertNull(bookmark.getName());
    assertEquals(Arrays.asList("1_29214"), bookmark.getStopIds());
    assertTrue(bookmark.getRouteFilter().getRouteIds().isEmpty());

    bookmark = bookmarks.get(1);
    assertEquals(1,bookmark.getId());
    assertNull(bookmark.getName());
    assertEquals(Arrays.asList("1_75403", "1_75414"), bookmark.getStopIds());
    assertTrue(bookmark.getRouteFilter().getRouteIds().isEmpty());
  }

  @Test
  public void testV3ToV2Migration() {

    UserPropertiesV3 v3 = new UserPropertiesV3();
    v3.setDefaultLocationLat(47.0);
    v3.setDefaultLocationLon(-122.0);
    v3.setDefaultLocationName("Seattle");
    v3.setRememberPreferencesEnabled(true);

    Bookmark b1 = new Bookmark(0,null,Arrays.asList("1_29214"),new RouteFilter());
    Bookmark b2 = new Bookmark(1,null,Arrays.asList("1_75403", "1_75414"), new RouteFilter());
    v3.setBookmarks(Arrays.asList(b1, b2));

    UserPropertiesV3 result = _service.migrate(v3, UserPropertiesV3.class);
    assertTrue(v3 == result);

    UserPropertiesV2 v2 = _service.migrate(v3, UserPropertiesV2.class);

    assertTrue(v2.isRememberPreferencesEnabled());

    assertEquals(47.0, v2.getDefaultLocationLat(), 0.0);
    assertEquals(-122.0, v2.getDefaultLocationLon(), 0.0);
    assertEquals("Seattle", v2.getDefaultLocationName());

    List<Bookmark> bookmarks = v2.getBookmarks();
    assertEquals(2, bookmarks.size());

    Bookmark bookmark = bookmarks.get(0);
    assertEquals(0,bookmark.getId());
    assertNull(bookmark.getName());
    assertEquals(Arrays.asList("1_29214"), bookmark.getStopIds());
    assertTrue(bookmark.getRouteFilter().getRouteIds().isEmpty());

    bookmark = bookmarks.get(1);
    assertEquals(1,bookmark.getId());
    assertNull(bookmark.getName());
    assertEquals(Arrays.asList("1_75403", "1_75414"), bookmark.getStopIds());
    assertTrue(bookmark.getRouteFilter().getRouteIds().isEmpty());
  }
}
