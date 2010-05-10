package org.onebusaway.users.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.client.model.UserIndexBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.UserRole;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserPropertiesMigration;
import org.onebusaway.users.services.UserPropertiesMigrationStatus;
import org.onebusaway.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceV1Impl implements UserService {

  private static Logger _log = LoggerFactory.getLogger(UserServiceV1Impl.class);

  private UserDao _userDao;

  private StandardAuthoritiesService _authoritiesService;

  private UserPropertiesMigration _userPropertiesMigration;

  @Autowired
  public void setUserDao(UserDao dao) {
    _userDao = dao;
  }

  @Autowired
  public void setAuthoritiesService(
      StandardAuthoritiesService authoritiesService) {
    _authoritiesService = authoritiesService;
  }

  @Autowired
  public void setUserPropertiesMigration(
      UserPropertiesMigration userPropertiesMigration) {
    _userPropertiesMigration = userPropertiesMigration;
  }

  @Override
  public UserBean getUserAsBean(User user) {

    UserBean bean = new UserBean();

    UserPropertiesV1 properties = getProperties(user);

    bean.setUserId(Integer.toString(user.getId()));
    bean.setRememberPreferencesEnabled(properties.isRememberPreferencesEnabled());

    bean.setHasDefaultLocation(properties.hasDefaultLocationLat()
        && properties.hasDefaultLocationLon());

    bean.setDefaultLocationName(properties.getDefaultLocationName());
    bean.setDefaultLocationLat(properties.getDefaultLocationLat());
    bean.setDefaultLocationLon(properties.getDefaultLocationLon());

    bean.setLastSelectedStopIds(Arrays.asList(properties.getLastSelectedStopId()));

    int bookmarkIndex = 0;
    for (String stopId : properties.getBookmarkedStopIds()) {
      BookmarkBean bookmark = new BookmarkBean();
      bookmark.setId(bookmarkIndex++);
      bookmark.setStopIds(Arrays.asList(stopId));
      bean.addBookmark(bookmark);
    }

    UserRole anonymous = _authoritiesService.getAnonymousRole();
    boolean isAnonymous = user.getRoles().contains(anonymous);
    bean.setAnonymous(isAnonymous);

    UserRole admin = _authoritiesService.getAdministratorRole();
    boolean isAdmin = user.getRoles().contains(admin);
    bean.setAdmin(isAdmin);

    List<UserIndexBean> indices = new ArrayList<UserIndexBean>();
    bean.setIndices(indices);

    for (UserIndex index : user.getUserIndices()) {
      UserIndexKey key = index.getId();

      UserIndexBean indexBean = new UserIndexBean();
      indexBean.setType(key.getType());
      indexBean.setValue(key.getValue());
      indices.add(indexBean);
    }

    return bean;
  }

  @Override
  public void setRememberUserPreferencesEnabled(User user,
      boolean rememberPreferencesEnabled) {
    UserPropertiesV1 properties = getProperties(user);
    properties.setRememberPreferencesEnabled(rememberPreferencesEnabled);
    if (!rememberPreferencesEnabled)
      properties.clear();
    _userDao.saveOrUpdateUser(user);
  }

  @Override
  public void setDefaultLocation(User user, String locationName, double lat,
      double lon) {

    UserPropertiesV1 properties = getProperties(user);

    if (!properties.isRememberPreferencesEnabled())
      return;

    properties.setDefaultLocationName(locationName);
    properties.setDefaultLocationLat(lat);
    properties.setDefaultLocationLon(lon);

    _userDao.saveOrUpdateUser(user);
  }

  @Override
  public void clearDefaultLocation(User user) {
    setDefaultLocation(user, null, Double.NaN, Double.NaN);
  }

  @Override
  public int addStopBookmark(User user, String name, List<String> stopIds,
      RouteFilter filter) {

    UserPropertiesV1 properties = getProperties(user);

    if (!properties.isRememberPreferencesEnabled())
      return -1;

    properties.getBookmarkedStopIds().addAll(stopIds);

    _userDao.saveOrUpdateUser(user);

    return properties.getBookmarkedStopIds().size() - 1;
  }

  @Override
  public void updateStopBookmark(User user, int id, String name,
      List<String> stopIds, RouteFilter routeFilter) {

    UserPropertiesV1 properties = getProperties(user);

    if (!properties.isRememberPreferencesEnabled())
      return;

    List<String> bookmarks = properties.getBookmarkedStopIds();
    if (0 <= id && id < bookmarks.size()) {
      bookmarks.set(id, stopIds.get(0));
      _userDao.saveOrUpdateUser(user);
    }
  }

  @Override
  public void deleteStopBookmarks(User user, int index) {
    UserPropertiesV1 properties = getProperties(user);

    // Why don't we have a check for stateless user here? If the user wants to
    // remove information, that's ok. Still not sure why this would be called
    // either way.
    if (!properties.isRememberPreferencesEnabled())
      _log.warn("Attempt to delete bookmark for stateless user.  They shouldn't have bookmarks in the first place.  User="
          + user.getId());

    List<String> bookmarkedStopIds = properties.getBookmarkedStopIds();
    bookmarkedStopIds.remove(index);
    _userDao.saveOrUpdateUser(user);
  }

  @Override
  public void setLastSelectedStopIds(User user, List<String> stopIds) {

    if (stopIds.isEmpty())
      return;

    String stopId = stopIds.get(0);

    UserPropertiesV1 properties = getProperties(user);

    if (!properties.isRememberPreferencesEnabled())
      return;

    if (!stopId.equals(properties.getLastSelectedStopId())) {
      properties.setLastSelectedStopId(stopId);
      _userDao.saveOrUpdateUser(user);
    }
  }

  @Override
  public void deleteUser(User user) {
    _userDao.deleteUser(user);
  }

  @Override
  public boolean isAnonymous(User user) {
    return user.getRoles().contains(_authoritiesService.getAnonymousRole());
  }

  @Override
  public boolean isAdministrator(User user) {
    return user.getRoles().contains(_authoritiesService.getAdministratorRole());
  }

  @Override
  public void enableAdminRoleForUser(User user, boolean onlyIfNoOtherAdmins) {

    UserRole adminRole = _authoritiesService.getUserRoleForName(StandardAuthoritiesService.ADMINISTRATOR);

    if (onlyIfNoOtherAdmins) {
      int count = _userDao.getNumberOfUsersWithRole(adminRole);
      if (count > 0)
        return;
    }

    Set<UserRole> roles = user.getRoles();

    if (roles.add(adminRole))
      _userDao.saveOrUpdateUser(user);
  }

  @Override
  public UserIndex getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials, boolean isAnonymous) {

    UserIndex userIndex = _userDao.getUserIndexForId(key);

    if (userIndex == null) {

      User user = new User();
      user.setCreationTime(new Date());
      user.setTemporary(true);
      user.setProperties(new UserPropertiesV1());
      Set<UserRole> roles = new HashSet<UserRole>();
      if (isAnonymous)
        roles.add(_authoritiesService.getAnonymousRole());
      else
        roles.add(_authoritiesService.getUserRole());
      user.setRoles(roles);
      _userDao.saveOrUpdateUser(user);

      userIndex = new UserIndex();
      userIndex.setId(key);
      userIndex.setCredentials(credentials);
      userIndex.setUser(user);

      user.addUserIndex(userIndex);

      _userDao.saveOrUpdateUserIndex(userIndex);
    }

    return userIndex;
  }

  @Override
  public UserIndex getUserIndexForId(UserIndexKey key) {
    return _userDao.getUserIndexForId(key);
  }

  @Override
  public void mergeUsers(User sourceUser, User targetUser) {

    if (sourceUser.getCreationTime().before(targetUser.getCreationTime()))
      targetUser.setCreationTime(sourceUser.getCreationTime());

    mergeProperties(getProperties(sourceUser), getProperties(targetUser));
    mergeRoles(sourceUser, targetUser);

    List<UserIndex> indices = new ArrayList<UserIndex>();

    for (UserIndex index : sourceUser.getUserIndices()) {
      UserIndex dup = new UserIndex();
      dup.setId(index.getId());
      dup.setCredentials(index.getCredentials());
      indices.add(dup);
    }

    deleteUser(sourceUser);

    for (UserIndex index : indices)
      targetUser.addUserIndex(index);

    _userDao.saveOrUpdateUser(targetUser);

    for (UserIndex index : indices)
      _userDao.saveOrUpdateUserIndex(index);
  }

  /****
   * Private Methods
   ****/

  private UserPropertiesV1 getProperties(User user) {
    return _userPropertiesMigration.migrate(user.getProperties(),
        UserPropertiesV1.class);
  }

  private void mergeRoles(User sourceUser, User targetUser) {
    Set<UserRole> roles = new HashSet<UserRole>();
    roles.addAll(sourceUser.getRoles());
    roles.addAll(targetUser.getRoles());

    UserRole anon = _authoritiesService.getAnonymousRole();
    UserRole user = _authoritiesService.getUserRole();
    if (roles.contains(user))
      roles.remove(anon);

    targetUser.setRoles(roles);
  }

  private void mergeProperties(UserPropertiesV1 sourceProps,
      UserPropertiesV1 destProps) {

    if (!destProps.isRememberPreferencesEnabled())
      return;

    if (!sourceProps.isRememberPreferencesEnabled()) {
      destProps.setRememberPreferencesEnabled(false);
      destProps.clear();
      return;
    }

    if (destProps.getLastSelectedStopId() == null)
      destProps.setLastSelectedStopId(sourceProps.getLastSelectedStopId());

    if (!destProps.hasDefaultLocationLat()) {
      destProps.setDefaultLocationLat(sourceProps.getDefaultLocationLat());
      destProps.setDefaultLocationLon(sourceProps.getDefaultLocationLon());
      destProps.setDefaultLocationName(sourceProps.getDefaultLocationName());
    }

    List<String> bookmarks = new ArrayList<String>();
    bookmarks.addAll(destProps.getBookmarkedStopIds());

    for (String stopId : sourceProps.getBookmarkedStopIds())
      bookmarks.add(stopId);

    destProps.setBookmarkedStopIds(bookmarks);
  }

  @Override
  public void startUserPropertiesMigration() {
    _userPropertiesMigration.startUserPropertiesBulkMigration(UserPropertiesV1.class);
  }

  @Override
  public UserPropertiesMigrationStatus getUserPropertiesMigrationStatus() {
    return _userPropertiesMigration.getUserPropertiesBulkMigrationStatus();
  }

}
