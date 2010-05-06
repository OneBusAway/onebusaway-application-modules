package org.onebusaway.users.services;

import org.onebusaway.users.client.model.UserBean;

public interface UserDataService {

  public boolean hasCurrentUser();

  public UserBean getCurrentUserAsBean();

  public void setDefaultLocationForCurrentUser(String locationName, double lat,
      double lon);

  public void setLastSelectedStopId(String stopId);

  public void addStopBookmark(String stopId) throws BookmarkException;

  public void deleteStopBookmarks(int index);

}
