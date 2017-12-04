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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.onebusaway.users.client.model.BookmarkBean;
import org.onebusaway.users.client.model.RouteFilterBean;
import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.model.properties.Bookmark;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.model.properties.UserPropertiesV2;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserPropertiesMigration;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.internal.LastSelectedStopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class UserPropertiesServiceV2Impl implements UserPropertiesService {

  private static Logger _log = LoggerFactory.getLogger(UserPropertiesServiceV2Impl.class);

  private UserDao _userDao;

  private UserPropertiesMigration _userPropertiesMigration;

  private LastSelectedStopService _lastSelectedStopService;

  @Autowired
  public void setUserDao(UserDao dao) {
    _userDao = dao;
  }

  @Autowired
  public void setUserPropertiesMigration(
      UserPropertiesMigration userPropertiesMigration) {
    _userPropertiesMigration = userPropertiesMigration;
  }

  @Autowired
  public void setLastSelectedStopService(
      LastSelectedStopService lastSelectedStopService) {
    _lastSelectedStopService = lastSelectedStopService;
  }

  @Override
  public Class<? extends UserProperties> getUserPropertiesType() {
    return UserPropertiesV2.class;
  }

  @Override
  public UserBean getUserAsBean(User user, UserBean bean) {

    UserPropertiesV2 properties = getProperties(user);

    bean.setRememberPreferencesEnabled(properties.isRememberPreferencesEnabled());

    bean.setHasDefaultLocation(properties.hasDefaultLocationLat()
        && properties.hasDefaultLocationLon());

    bean.setDefaultLocationName(properties.getDefaultLocationName());
    bean.setDefaultLocationLat(properties.getDefaultLocationLat());
    bean.setDefaultLocationLon(properties.getDefaultLocationLon());

    List<String> stopIds = _lastSelectedStopService.getLastSelectedStopsForUser(user.getId());
    bean.setLastSelectedStopIds(stopIds);

    for (Bookmark bookmark : properties.getBookmarks()) {
      BookmarkBean bookmarkBean = new BookmarkBean();
      bookmarkBean.setId(bookmark.getId());
      bookmarkBean.setName(bookmark.getName());
      bookmarkBean.setStopIds(bookmark.getStopIds());
      bookmarkBean.setRouteFilter(getRouteFilterAsBean(bookmark.getRouteFilter()));
      bean.addBookmark(bookmarkBean);
    }

    bean.setMinApiRequestInterval(properties.getMinApiRequestInterval());
    
    Map<String, Long> readServiceAlerts = properties.getReadSituationIdsWithReadTime();
    if( readServiceAlerts == null)
      readServiceAlerts = Collections.emptyMap();
    bean.setReadServiceAlerts(readServiceAlerts);

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
    UserPropertiesV2 properties = getProperties(user);
    properties.setRememberPreferencesEnabled(rememberPreferencesEnabled);
    if (!rememberPreferencesEnabled)
      properties.clear();
    _userDao.saveOrUpdateUser(user);
  }

  @Override
  public void setDefaultLocation(User user, String locationName, double lat,
      double lon) {

    UserPropertiesV2 properties = getProperties(user);

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

    UserPropertiesV2 properties = getProperties(user);

    if (!properties.isRememberPreferencesEnabled())
      return -1;

    int maxId = 0;
    for (Bookmark bookmark : properties.getBookmarks())
      maxId = Math.max(maxId, bookmark.getId() + 1);

    Bookmark bookmark = new Bookmark(maxId, name, stopIds, filter);
    properties.getBookmarks().add(bookmark);

    _userDao.saveOrUpdateUser(user);

    return bookmark.getId();
  }

  @Override
  public void updateStopBookmark(User user, int id, String name,
      List<String> stopIds, RouteFilter routeFilter) {

    UserPropertiesV2 properties = getProperties(user);

    if (!properties.isRememberPreferencesEnabled())
      return;

    List<Bookmark> bookmarks = properties.getBookmarks();

    for (int index = 0; index < bookmarks.size(); index++) {
      Bookmark bookmark = bookmarks.get(index);
      if (bookmark.getId() == id) {
        bookmark = new Bookmark(id, name, stopIds, routeFilter);
        bookmarks.set(index, bookmark);
        _userDao.saveOrUpdateUser(user);
        return;
      }
    }
  }

  @Override
  public void resetUser(User user) {
    user.setProperties(new UserPropertiesV2());
    _userDao.saveOrUpdateUser(user);
    _lastSelectedStopService.clearLastSelectedStopForUser(user.getId());
  }

  @Override
  public void deleteStopBookmarks(User user, int id) {
    UserPropertiesV2 properties = getProperties(user);

    // Why don't we have a check for stateless user here? If the user wants to
    // remove information, that's ok. Still not sure why this would be called
    // either way.
    if (!properties.isRememberPreferencesEnabled())
      _log.warn("Attempt to delete bookmark for stateless user.  They shouldn't have bookmarks in the first place.  User="
          + user.getId());

    boolean modified = false;

    for (Iterator<Bookmark> it = properties.getBookmarks().iterator(); it.hasNext();) {
      Bookmark bookmark = it.next();
      if (bookmark.getId() == id) {
        it.remove();
        modified = true;
      }
    }

    if (modified)
      _userDao.saveOrUpdateUser(user);
  }

  @Override
  public void setLastSelectedStopIds(User user, List<String> stopIds) {
    _lastSelectedStopService.setLastSelectedStopsForUser(user.getId(), stopIds);
  }

  @Override
  public void authorizeApi(User user, long minApiRequestInterval) {
    UserPropertiesV2 properties = getProperties(user);
    properties.setMinApiRequestInterval(minApiRequestInterval);
    _userDao.saveOrUpdateUser(user);
  }

  @Override
  public void markServiceAlertAsRead(User user, String situationId, long time,
      boolean isRead) {

    UserPropertiesV2 properties = getProperties(user);
    Map<String, Long> readSituationIdsWithReadTime = properties.getReadSituationIdsWithReadTime();

    if (isRead) {

      if (readSituationIdsWithReadTime == null) {
        readSituationIdsWithReadTime = new HashMap<String, Long>();
        properties.setReadSituationIdsWithReadTime(readSituationIdsWithReadTime);
      }
      readSituationIdsWithReadTime.put(situationId, time);
      _userDao.saveOrUpdateUser(user);
    } else {
      if (readSituationIdsWithReadTime == null)
        return;
      if (readSituationIdsWithReadTime.remove(situationId) != null)
        _userDao.saveOrUpdateUser(user);
    }
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

  @Override
  public void mergeProperties(User sourceUser, User targetUser) {
    mergeProperties(getProperties(sourceUser), getProperties(targetUser));
  }

  /****
   * Private Methods
   ****/

  private UserPropertiesV2 getProperties(User user) {
    UserProperties props = user.getProperties();
    UserPropertiesV2 v2 = _userPropertiesMigration.migrate(props,
        UserPropertiesV2.class);
    if (props != v2)
      user.setProperties(v2);
    return v2;
  }

  private void mergeProperties(UserPropertiesV2 sourceProps,
      UserPropertiesV2 destProps) {

    if (!destProps.isRememberPreferencesEnabled())
      return;

    if (!sourceProps.isRememberPreferencesEnabled()) {
      destProps.setRememberPreferencesEnabled(false);
      destProps.clear();
      return;
    }

    if (!destProps.hasDefaultLocationLat()) {
      destProps.setDefaultLocationLat(sourceProps.getDefaultLocationLat());
      destProps.setDefaultLocationLon(sourceProps.getDefaultLocationLon());
      destProps.setDefaultLocationName(sourceProps.getDefaultLocationName());
    }

    List<Bookmark> bookmarks = new ArrayList<Bookmark>();
    bookmarks.addAll(destProps.getBookmarks());
    bookmarks.addAll(sourceProps.getBookmarks());
    destProps.setBookmarks(bookmarks);
  }

  private RouteFilterBean getRouteFilterAsBean(RouteFilter routeFilter) {
    return new RouteFilterBean(routeFilter.getRouteIds());
  }
}
