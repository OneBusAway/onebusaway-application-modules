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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserPropertiesMigration;
import org.onebusaway.users.services.UserPropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class UserPropertiesServiceV1Impl implements UserPropertiesService {

  private static Logger _log = LoggerFactory.getLogger(UserPropertiesServiceV1Impl.class);

  private UserDao _userDao;

  private UserPropertiesMigration _userPropertiesMigration;

  @Autowired
  public void setUserDao(UserDao dao) {
    _userDao = dao;
  }

  @Autowired
  public void setUserPropertiesMigration(
      UserPropertiesMigration userPropertiesMigration) {
    _userPropertiesMigration = userPropertiesMigration;
  }

  @Override
  public Class<? extends UserProperties> getUserPropertiesType() {
    return UserPropertiesV1.class;
  }

  @Override
  public UserBean getUserAsBean(User user, UserBean bean) {

    UserPropertiesV1 properties = getProperties(user);

    bean.setRememberPreferencesEnabled(properties.isRememberPreferencesEnabled());

    bean.setHasDefaultLocation(properties.hasDefaultLocationLat()
        && properties.hasDefaultLocationLon());

    bean.setDefaultLocationName(properties.getDefaultLocationName());
    bean.setDefaultLocationLat(properties.getDefaultLocationLat());
    bean.setDefaultLocationLon(properties.getDefaultLocationLon());

    if (properties.getLastSelectedStopId() != null)
      bean.setLastSelectedStopIds(Arrays.asList(properties.getLastSelectedStopId()));

    int bookmarkIndex = 0;
    for (String stopId : properties.getBookmarkedStopIds()) {
      BookmarkBean bookmark = new BookmarkBean();
      bookmark.setId(bookmarkIndex++);
      bookmark.setStopIds(Arrays.asList(stopId));
      bean.addBookmark(bookmark);
    }

    return bean;
  }

  @Override
  public UserBean getAnonymousUserAsBean(UserBean bean) {
    bean.setRememberPreferencesEnabled(true);
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
  public void resetUser(User user) {
    user.setProperties(new UserPropertiesV1());
    _userDao.saveOrUpdateUser(user);
  }

  @Override
  public void mergeProperties(User sourceUser, User targetUser) {
    mergeProperties(getProperties(sourceUser), getProperties(targetUser));
  }

  /****
   * Private Methods
   ****/

  private UserPropertiesV1 getProperties(User user) {
    return _userPropertiesMigration.migrate(user.getProperties(),
        UserPropertiesV1.class);
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
  public void authorizeApi(User user, long minRequestInteval) {
    throw new IllegalStateException("V1 user properties don't support api keys");
  }

  @Override
  public void markServiceAlertAsRead(User user, String situationId, long time,
      boolean isRead) {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public void updateApiKeyContactInfo(User user, String contactName, 
      String contactCompany, String contactEmail, String contactDetails) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void disableUser(User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void activateUser(User user) {
      throw new UnsupportedOperationException();
  }

}
