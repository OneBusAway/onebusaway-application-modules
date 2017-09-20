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

import java.util.Arrays;

import javax.annotation.PreDestroy;

import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.properties.Bookmark;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.model.properties.UserPropertiesV2;
import org.onebusaway.users.model.properties.UserPropertiesV3;
import org.onebusaway.users.model.properties.UserPropertiesV4;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserPropertiesMigration;
import org.onebusaway.users.services.UserPropertiesMigrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserPropertiesMigrationImpl implements UserPropertiesMigration {

    private static Logger _log = LoggerFactory.getLogger(UserPropertiesMigrationImpl.class);
  private Object _userPropertiesMigrationOperationLock = new Object();

  private UserPropertiesMigrationBulkOperation<?> _operation = null;

  private UserDao _userDao;

  @Autowired
  public void setUserDao(UserDao userDao) {
    _userDao = userDao;
  }

  @PreDestroy
  public void stop() {
    if (_operation != null)
      _operation.cancel();
    _operation = null;
  }

  @Override
  public boolean needsMigration(UserProperties properties, Class<?> target) {
    return !target.isAssignableFrom(properties.getClass());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends UserProperties> T migrate(UserProperties properties,
      Class<T> target) {

    if (target.isAssignableFrom(properties.getClass()))
      return (T) properties;

    //if you are migrating from V1 to V2, V3 or V4
    if (UserPropertiesV1.class.isAssignableFrom(properties.getClass())) {
        if (target == UserPropertiesV2.class) {
            return (T) getV2Properties((UserPropertiesV1) properties);
        } else if (target == UserPropertiesV3.class) {
            properties = getV2Properties((UserPropertiesV1) properties);
            return (T) getV3Properties((UserPropertiesV2) properties);
        } else if (target == UserPropertiesV4.class) {
            properties = getV2Properties((UserPropertiesV1) properties);
            properties = getV3Properties((UserPropertiesV2) properties);
            return (T) getV4Properties((UserPropertiesV3) properties);
        }
    }

    //if you are migrating from V2 to V1, V3 or V4
    if (UserPropertiesV2.class.isAssignableFrom(properties.getClass())) {
      if (target == UserPropertiesV1.class) {
        return (T) getV1Properties((UserPropertiesV2) properties);
      } else if (target == UserPropertiesV3.class) {
        return (T) getV3Properties((UserPropertiesV2) properties);
      } else if (target == UserPropertiesV4.class) {
          properties = getV3Properties((UserPropertiesV2) properties);
          return (T) getV4Properties((UserPropertiesV3) properties);
      }
    }

    //if you are migrating from V3 to V1, V2 or V4
    if (UserPropertiesV3.class.isAssignableFrom(properties.getClass())) {
      if (target == UserPropertiesV1.class) {
        properties = getV2PropertiesFromV3((UserPropertiesV3) properties);
        return (T) getV1Properties((UserPropertiesV2) properties);
      } else if (target == UserPropertiesV2.class) {
        return (T) getV2PropertiesFromV3((UserPropertiesV3) properties);
      } else if (target == UserPropertiesV4.class) {
          return (T) getV4Properties((UserPropertiesV3) properties);
      }
    }

      //if you are migrating from V4 to V1, V2 or V3
      if (UserPropertiesV4.class.isAssignableFrom(properties.getClass())) {
          if (target == UserPropertiesV1.class) {
              properties = getV3PropertiesFromV4((UserPropertiesV4) properties);
              properties = getV2PropertiesFromV3((UserPropertiesV3) properties);
              return (T) getV1Properties((UserPropertiesV2) properties);
          } else if (target == UserPropertiesV2.class) {
              properties = getV3PropertiesFromV4((UserPropertiesV4) properties);
              return (T) getV2PropertiesFromV3((UserPropertiesV3) properties);
          } else if (target == UserPropertiesV3.class) {
              return (T) getV3PropertiesFromV4((UserPropertiesV4) properties);
          }
      }

    throw new IllegalStateException("can't convert properties: from="
        + properties.getClass() + " to=" + target);
  }

  @Override
  public <T extends UserProperties> void startUserPropertiesBulkMigration(
      Class<T> target) {
    if (_operation == null || _operation.isCanceled()
        || _operation.isComplete()) {
      if (_operation != null)
        _operation.cancel();
      _operation = UserPropertiesMigrationBulkOperation.execute(_userDao, this,
          target);
    }
  }

  @Override
  public UserPropertiesMigrationStatus getUserPropertiesBulkMigrationStatus() {
    synchronized (_userPropertiesMigrationOperationLock) {
      if (_operation != null)
        return _operation.getStatus();
      return null;
    }
  }

  /****
   * 
   * param UserPropertiesVersion
   * return
   */

  private UserPropertiesV4 getV4Properties(UserPropertiesV3 v3) {

      UserPropertiesV4 v4 = new UserPropertiesV4();

      v4.setRememberPreferencesEnabled(v3.isRememberPreferencesEnabled());

      v4.setDefaultLocationLat(v3.getDefaultLocationLat());
      v4.setDefaultLocationLon(v3.getDefaultLocationLon());
      v4.setDefaultLocationName(v3.getDefaultLocationName());
      v4.setBookmarks(v3.getBookmarks());
      v4.setMinApiRequestInterval(v3.getMinApiRequestInterval());
      v4.setReadSituationIdsWithReadTime(v3.getReadSituationIdsWithReadTime());
      v4.setContactCompany(v3.getContactCompany());
      v4.setContactDetails(v3.getContactDetails());
      v4.setContactName(v3.getContactName());
      v4.setContactEmail(v3.getContactEmail());

      return v4;
  }

  private UserPropertiesV3 getV3PropertiesFromV4(UserPropertiesV4 v4) {

    UserPropertiesV3 v3 = new UserPropertiesV3();

    v3.setRememberPreferencesEnabled(v4.isRememberPreferencesEnabled());

    v3.setDefaultLocationLat(v4.getDefaultLocationLat());
    v3.setDefaultLocationLon(v4.getDefaultLocationLon());
    v3.setDefaultLocationName(v4.getDefaultLocationName());
    v3.setBookmarks(v4.getBookmarks());
    v3.setMinApiRequestInterval(v4.getMinApiRequestInterval());
    v3.setReadSituationIdsWithReadTime(v4.getReadSituationIdsWithReadTime());

    return v3;
  }

  private UserPropertiesV3 getV3Properties(UserPropertiesV2 v2) {

    UserPropertiesV3 v3 = new UserPropertiesV3();

    v3.setRememberPreferencesEnabled(v2.isRememberPreferencesEnabled());

    v3.setDefaultLocationLat(v2.getDefaultLocationLat());
    v3.setDefaultLocationLon(v2.getDefaultLocationLon());
    v3.setDefaultLocationName(v2.getDefaultLocationName());
    v3.setBookmarks(v2.getBookmarks());
    v3.setMinApiRequestInterval(v2.getMinApiRequestInterval());
    v3.setReadSituationIdsWithReadTime(v2.getReadSituationIdsWithReadTime());

    return v3;
    }

  private UserPropertiesV2 getV2PropertiesFromV3(UserPropertiesV3 v3) {

    UserPropertiesV2 v2 = new UserPropertiesV2();

    v2.setRememberPreferencesEnabled(v3.isRememberPreferencesEnabled());

    v2.setDefaultLocationLat(v3.getDefaultLocationLat());
    v2.setDefaultLocationLon(v3.getDefaultLocationLon());
    v2.setDefaultLocationName(v3.getDefaultLocationName());
    v2.setBookmarks(v3.getBookmarks());
    v2.setMinApiRequestInterval(v3.getMinApiRequestInterval());
    v2.setReadSituationIdsWithReadTime(v3.getReadSituationIdsWithReadTime());

    return v2;
  }

  private UserPropertiesV2 getV2Properties(UserPropertiesV1 v1) {

    UserPropertiesV2 v2 = new UserPropertiesV2();

    v2.setRememberPreferencesEnabled(v1.isRememberPreferencesEnabled());

    v2.setDefaultLocationLat(v1.getDefaultLocationLat());
    v2.setDefaultLocationLon(v1.getDefaultLocationLon());
    v2.setDefaultLocationName(v1.getDefaultLocationName());

    int index = 0;
    for (String stopId : v1.getBookmarkedStopIds()) {
      Bookmark bookmark = new Bookmark(index++,null,Arrays.asList(stopId),new RouteFilter());
      v2.getBookmarks().add(bookmark);
    }

    return v2;
  }

  private UserPropertiesV1 getV1Properties(UserPropertiesV2 v2) {

    UserPropertiesV1 v1 = new UserPropertiesV1();

    v1.setRememberPreferencesEnabled(v2.isRememberPreferencesEnabled());

    v1.setDefaultLocationLat(v2.getDefaultLocationLat());
    v1.setDefaultLocationLon(v2.getDefaultLocationLon());
    v1.setDefaultLocationName(v2.getDefaultLocationName());

    for (Bookmark bookmark : v2.getBookmarks())
      v1.getBookmarkedStopIds().addAll(bookmark.getStopIds());

    return v1;
  }

}
