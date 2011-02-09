package org.onebusaway.users.services;

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserProperties;
import org.onebusaway.users.model.properties.RouteFilter;

public interface UserPropertiesService {

  public Class<? extends UserProperties> getUserPropertiesType();

  public UserBean getUserAsBean(User user, UserBean bean);

  public UserBean getAnonymousUserAsBean(UserBean bean);

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

  /**
   * Authorize this user to use the api
   * 
   * @param User the user
   * @param minApiRequestInteval the minimum time between requests in
   *          milliseconds
   */
  public void authorizeApi(User user, long minApiRequestInteval);

  public void markServiceAlertAsRead(User user, String situationId, long time,
      boolean isRead);
  
  public void resetUser(User user);

  public void mergeProperties(User sourceUser, User targetUser);
}
