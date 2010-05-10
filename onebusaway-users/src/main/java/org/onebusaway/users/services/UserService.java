package org.onebusaway.users.services;

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.properties.RouteFilter;

public interface UserService {

  public UserIndex getUserIndexForId(UserIndexKey key);

  public UserIndex getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials, boolean isAnonymous);

  public UserBean getUserAsBean(User user);

  public void setRememberUserPreferencesEnabled(User user,
      boolean rememberUserPreferencesEnabled);

  public void setDefaultLocation(User user, String locationName, double lat,
      double lon);

  public void clearDefaultLocation(User user);

  public void setLastSelectedStopIds(User user, List<String> stopId);

  /**
   * 
   * @param user
   * @param name
   * @param stopIds
   * @param filter
   * @return the id for the newly created bookmark
   */
  public int addStopBookmark(User user, String name, List<String> stopIds,
      RouteFilter filter);

  public void updateStopBookmark(User user, int id, String name,
      List<String> stopIds, RouteFilter routeFilter);

  public void deleteStopBookmarks(User user, int id);

  public void deleteUser(User user);

  public boolean isAnonymous(User user);

  public boolean isAdministrator(User user);

  public void enableAdminRoleForUser(User user, boolean onlyIfNoOtherAdmins);

  public void mergeUsers(User sourceUser, User targetUser);

  public void startUserPropertiesMigration();

  public UserPropertiesMigrationStatus getUserPropertiesMigrationStatus();

}
