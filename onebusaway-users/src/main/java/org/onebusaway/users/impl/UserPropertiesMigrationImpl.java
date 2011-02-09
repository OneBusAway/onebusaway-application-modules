package org.onebusaway.users.impl;

import java.util.Arrays;

import javax.annotation.PreDestroy;

import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.model.properties.Bookmark;
import org.onebusaway.users.model.properties.RouteFilter;
import org.onebusaway.users.model.properties.UserPropertiesV2;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserPropertiesMigration;
import org.onebusaway.users.services.UserPropertiesMigrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserPropertiesMigrationImpl implements UserPropertiesMigration {

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

    if (UserPropertiesV1.class.isAssignableFrom(properties.getClass())
        && target == UserPropertiesV2.class)
      return (T) getV2Properties((UserPropertiesV1) properties);

    if (UserPropertiesV2.class.isAssignableFrom(properties.getClass())
        && target == UserPropertiesV1.class)
      return (T) getV1Properties((UserPropertiesV2) properties);

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
   * @param v1
   * @return
   */

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
