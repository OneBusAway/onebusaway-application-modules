package org.onebusaway.users.services;

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.properties.RouteFilter;

public interface CurrentUserService {

  public void handleLogin(String type, String id);

  public boolean hasCurrentUser();

  public UserBean getCurrentUser();

  public void setRememberUserPreferencesEnabled(
      boolean rememberUserPreferencesEnabled);

  public void setDefaultLocation(String locationName, double lat, double lon);

  public void clearDefaultLocation();

  public void setLastSelectedStopIds(List<String> stopIds);

  /**
   * @param name
   * @param stopIds
   * @param filter
   * @return the newly created bookmark id
   */
  public int addStopBookmark(String name, List<String> stopIds, RouteFilter filter);
  
  public void updateStopBookmark(int id, String name, List<String> stopIds,
      RouteFilter routeFilter);

  public void deleteStopBookmarks(int id);

  public void deleteCurrentUser();

  public void enableAdminRole();


}
