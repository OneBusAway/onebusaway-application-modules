package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.services.BookmarkException;

public class UserServiceV1ImplTest {

  private SessionFactory _sessionFactory;

  private UserServiceV1Impl _service;

  private UserDaoImpl _dao;

  private StandardAuthoritiesServiceImpl _authoritiesService;

  @Before
  public void setup() throws IOException {

    Configuration config = new AnnotationConfiguration();
    config = config.configure("org/onebusaway/users/hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();

    _dao = new UserDaoImpl();
    _dao.setSessionFactory(_sessionFactory);

    _authoritiesService = new StandardAuthoritiesServiceImpl();
    _authoritiesService.setUserDao(_dao);
    _authoritiesService.bootstrap();

    _service = new UserServiceV1Impl();
    _service.setUserDao(_dao);
    _service.setAuthoritiesService(_authoritiesService);
    _service.setUserPropertiesMigration(new UserPropertiesMigrationImpl());
  }

  @After
  public void teardown() {
    if (_sessionFactory != null)
      _sessionFactory.close();
  }

  @Test
  public void testGetOrCreateUserForIndexKey() {

    UserIndexKey key = new UserIndexKey("phone", "1234");
    UserIndex userIndex = _service.getOrCreateUserForIndexKey(key, "pass", true);
    User user = userIndex.getUser();

    assertEquals(key, userIndex.getId());
    assertEquals(user, userIndex.getUser());
    assertEquals("pass", userIndex.getCredentials());

    assertEquals(1, user.getUserIndices().size());
    assertTrue(user.getUserIndices().contains(userIndex));

    int count = _dao.getNumberOfUsersWithRole(_authoritiesService.getAnonymousRole());
    assertEquals(1, count);

    count = _dao.getNumberOfUsersWithRole(_authoritiesService.getUserRole());
    assertEquals(0, count);

    count = _dao.getNumberOfUsersWithRole(_authoritiesService.getAdministratorRole());
    assertEquals(0, count);

    UserIndex userIndexB = _dao.getUserIndexForId(key);
    assertEquals(userIndexB.getId(), userIndex.getId());
    assertEquals(userIndexB.getCredentials(), userIndex.getCredentials());
    assertEquals(userIndexB.getUser(), userIndex.getUser());
  }

  @Test
  public void testAddStopBookmark() throws BookmarkException {

    UserIndexKey key = new UserIndexKey("phone", "1234");
    UserIndex userIndex = _service.getOrCreateUserForIndexKey(key, "pass", true);
    User user = userIndex.getUser();

    _service.addStopBookmark(user, "bookmkark", Arrays.asList("1"),
        new RouteFilter());

    List<String> bookmarks = getProperties(user).getBookmarkedStopIds();
    assertEquals(1, bookmarks.size());
    assertTrue(bookmarks.contains("1"));
  }

  private UserPropertiesV1 getProperties(User user) {
    return (UserPropertiesV1) user.getProperties();
  }

  @Test
  public void testDeleteUser() {

    UserIndexKey key = new UserIndexKey("phone", "1234");
    UserIndex userIndex = _service.getOrCreateUserForIndexKey(key, "pass", true);
    User user = userIndex.getUser();

    _service.deleteUser(user);

    User u = _dao.getUserForId(user.getId());
    assertNull(u);

    int count = _dao.getNumberOfUsersWithRole(_authoritiesService.getAnonymousRole());
    assertEquals(0, count);

    UserIndex in = _dao.getUserIndexForId(key);
    assertNull(in);
  }

  @Test
  public void testMergeUsers01() throws BookmarkException {

    UserIndexKey keyA = new UserIndexKey("phone", "1234");
    UserIndex userIndexA = _service.getOrCreateUserForIndexKey(keyA, "pass",
        true);
    User userA = userIndexA.getUser();

    _service.setLastSelectedStopIds(userA, Arrays.asList("A"));
    _service.addStopBookmark(userA, "bookmark a", Arrays.asList("A", "C"),
        new RouteFilter());

    UserIndexKey keyB = new UserIndexKey("phone", "5678");
    UserIndex userIndexB = _service.getOrCreateUserForIndexKey(keyB, "pass",
        false);
    User userB = userIndexB.getUser();

    _service.setDefaultLocation(userB, "here", 47.0, -122.0);
    _service.addStopBookmark(userB, "bookmark b", Arrays.asList("C", "B"),
        new RouteFilter());
    _service.mergeUsers(userA, userB);

    User u = _dao.getUserForId(userA.getId());
    assertNull(u);

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

    assertEquals(1, userB.getRoles().size());
    assertTrue(userB.getRoles().contains(_authoritiesService.getUserRole()));

    assertEquals(2, userB.getUserIndices().size());

    UserIndex index = _service.getUserIndexForId(keyA);
    assertEquals(userB, index.getUser());
    assertTrue(userB.getUserIndices().contains(index));

    index = _service.getUserIndexForId(keyB);
    assertEquals(userB, index.getUser());
    assertTrue(userB.getUserIndices().contains(index));
  }

  @Test
  public void testMergeUsers02() throws BookmarkException {

    UserIndexKey keyA = new UserIndexKey("phone", "1234");
    UserIndex userIndexA = _service.getOrCreateUserForIndexKey(keyA, "pass",
        false);
    User userA = userIndexA.getUser();

    _service.setLastSelectedStopIds(userA, Arrays.asList("A"));
    _service.setDefaultLocation(userA, "here", 47.0, -122.0);
    _service.addStopBookmark(userA, "bookmark A", Arrays.asList("A", "C"),
        new RouteFilter());

    UserIndexKey keyB = new UserIndexKey("phone", "5678");
    UserIndex userIndexB = _service.getOrCreateUserForIndexKey(keyB, "pass",
        false);
    User userB = userIndexB.getUser();

    _service.enableAdminRoleForUser(userA, false);

    _service.mergeUsers(userA, userB);

    User u = _dao.getUserForId(userA.getId());
    assertNull(u);

    UserPropertiesV1 props = getProperties(userB);
    List<String> bookmarks = props.getBookmarkedStopIds();
    assertEquals(2, bookmarks.size());
    assertEquals("A", bookmarks.get(0));
    assertEquals("C", bookmarks.get(1));

    assertEquals("here", props.getDefaultLocationName());
    assertEquals(47.0, props.getDefaultLocationLat(), 0.0);
    assertEquals(-122.0, props.getDefaultLocationLon(), 0.0);

    assertEquals("A", props.getLastSelectedStopId());

    assertEquals(2, userB.getRoles().size());
    assertTrue(userB.getRoles().contains(_authoritiesService.getUserRole()));
    assertTrue(userB.getRoles().contains(
        _authoritiesService.getAdministratorRole()));
  }

  @Test
  public void testMergeUsers03() throws BookmarkException {

    UserIndexKey keyA = new UserIndexKey("phone", "1234");
    UserIndex userIndexA = _service.getOrCreateUserForIndexKey(keyA, "pass",
        true);
    User userA = userIndexA.getUser();

    _service.setLastSelectedStopIds(userA, Arrays.asList("A"));
    _service.setDefaultLocation(userA, "here", 47.0, -122.0);
    _service.addStopBookmark(userA, "bookmark a", Arrays.asList("A", "C"),
        new RouteFilter());

    UserIndexKey keyB = new UserIndexKey("phone", "5678");
    UserIndex userIndexB = _service.getOrCreateUserForIndexKey(keyB, "pass",
        false);
    User userB = userIndexB.getUser();

    _service.setLastSelectedStopIds(userB, Arrays.asList("B"));
    _service.setDefaultLocation(userB, "there", 48.0, -123.0);
    _service.addStopBookmark(userB, "bookmark b", Arrays.asList("B", "A"),
        new RouteFilter());

    _service.mergeUsers(userA, userB);

    User u = _dao.getUserForId(userA.getId());
    assertNull(u);

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
}
