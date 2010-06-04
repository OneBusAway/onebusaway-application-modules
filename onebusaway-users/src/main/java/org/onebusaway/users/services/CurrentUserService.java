package org.onebusaway.users.services;

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.impl.authentication.AutoUserProcessingFilter;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.properties.RouteFilter;

public interface CurrentUserService {

  public boolean hasCurrentUser();

  /**
   * @return the current user. If no user is currently logged in, we return an
   *         anonymous user
   */
  public UserBean getCurrentUser();

  /**
   * @param useAnonymousUser if no user is logged in and {@code
   *          useAnonymousUser} is true, we return an anonymous user object
   * @return the current user
   */
  public UserIndex getCurrentUserAsUserIndex(boolean useAnonymousUser);

  /**
   * @return true if the current user is anonymous or if there is no current
   *         user
   */
  public boolean isCurrentUserAnonymous();

  /**
   * 
   * @param type the {@link UserIndexKey} type
   * @param id the {@link UserIndexKey} id
   * @param credentials {@link UserIndex} credentials
   * @param registerIfNewUser if true, automatically register a new user if one
   *          does not exist already
   * @return true if the user was successfully logged in, otherwise false
   */
  public boolean handleLogin(String type, String id, String credentials,
      boolean registerIfNewUser);

  public void handleAddAccount(String type, String id, String credentials);

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
  public int addStopBookmark(String name, List<String> stopIds,
      RouteFilter filter);

  public void updateStopBookmark(int id, String name, List<String> stopIds,
      RouteFilter routeFilter);

  public void deleteStopBookmarks(int id);

  /**
   * @param phoneNumber the phone number to register to the current user
   * @return the registration code that must be used validate the phoneNumber
   */
  public String registerPhoneNumber(String phoneNumber);

  public boolean hasPhoneNumberRegistration();

  /**
   * 
   * @param registrationCode
   * @return true if the registration was successful, otherwise false
   */
  public boolean completePhoneNumberRegistration(String registrationCode);

  public void clearPhoneNumberRegistration();

  public void removeUserIndex(UserIndexKey key);

  public void deleteCurrentUser();

  public void resetCurrentUser();

  public void enableAdminRole();

}
