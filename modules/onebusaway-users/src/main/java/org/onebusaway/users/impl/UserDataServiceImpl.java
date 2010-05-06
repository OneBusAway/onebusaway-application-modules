package org.onebusaway.users.impl;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserPropertiesV1;
import org.onebusaway.users.services.BookmarkException;
import org.onebusaway.users.services.BookmarksAtCapacityException;
import org.onebusaway.users.services.LocationAlreadyBookmarkedException;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDataServiceImpl implements UserDataService {

  private UserDao _userDao;

  @Autowired
  public void setUserDao(UserDao dao) {
    _userDao = dao;
  }

  public boolean hasCurrentUser() {
    return getCurrentUser() != null;
  }

  public UserBean getCurrentUserAsBean() {

    User user = getCurrentUser();

    if (user == null)
      return null;

    UserBean bean = new UserBean();

    UserPropertiesV1 properties = user.getProperties();

    bean.setHasDefaultLocation(properties.hasDefaultLocationLat()
        && properties.hasDefaultLocationLon());

    bean.setDefaultLocationName(properties.getDefaultLocationName());
    bean.setDefautLocationLat(properties.getDefaultLocationLat());
    bean.setDefaultLocationLon(properties.getDefaultLocationLon());

    bean.setLastSelectedStopId(properties.getLastSelectedStopId());

    bean.setBookmarkedStopIds(properties.getBookmarkedStopIds());

    return bean;
  }

  public void setDefaultLocationForCurrentUser(String locationName, double lat,
      double lon) {

    User user = getCurrentUser();
    UserPropertiesV1 properties = user.getProperties();

    properties.setDefaultLocationName(locationName);
    properties.setDefaultLocationLat(lat);
    properties.setDefaultLocationLon(lon);

    _userDao.saveOrUpdateUser(user);
  }

  public void addStopBookmark(String stopId) throws BookmarkException {

    User user = getCurrentUser();
    UserPropertiesV1 properties = user.getProperties();

    if (properties.getBookmarkedStopIds().size() == 9)
      throw new BookmarksAtCapacityException();

    if (properties.getBookmarkedStopIds().contains(stopId))
      throw new LocationAlreadyBookmarkedException();

    properties.getBookmarkedStopIds().add(stopId);

    _userDao.saveOrUpdateUser(user);
  }

  public void deleteStopBookmarks(int index) {
    User user = getCurrentUser();
    UserPropertiesV1 properties = user.getProperties();
    List<String> bookmarkedStopIds = properties.getBookmarkedStopIds();
    bookmarkedStopIds.remove(index);
    _userDao.saveOrUpdateUser(user);
  }

  public void setLastSelectedStopId(String stopId) {
    User user = getCurrentUser();
    UserPropertiesV1 properties = user.getProperties();
    properties.setLastSelectedStopId(stopId);
    _userDao.saveOrUpdateUser(user);
  }

  /****
   * Private Methods
   ****/

  private User getCurrentUser() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null)
      return null;
    Object details = authentication.getDetails();
    if (details == null)
      return null;
    if (!(details instanceof IndexedUserDetails))
      return null;

    IndexedUserDetails wrapper = (IndexedUserDetails) details;
    UserIndex userIndex = wrapper.getUserIndex();
    return userIndex.getUser();
  }

}
