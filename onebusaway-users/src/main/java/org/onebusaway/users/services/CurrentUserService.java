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
package org.onebusaway.users.services;

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.properties.RouteFilter;

/**
 * Service methods for performing operations on the currently logged-in user.
 * 
 * @author bdferris
 * 
 */
public interface CurrentUserService {

  public static final String MODE_LOGIN = "login";

  public static final String MODE_REGISTRATION = "registration";

  public static final String MODE_ADD_ACCOUNT = "add-account";

  /**
   * @return the current user's details, or null if no user is currently logged
   *         in
   */
  public IndexedUserDetails getCurrentUserDetails();

  /**
   * @return the current user, or null if no user is currently logged in
   */
  public UserBean getCurrentUser();

  /**
   * @return the current user, creating the user as appropriate or returning
   *         null if no user is logged in or could not be created
   */
  public UserBean getCurrentUser(boolean createUserIfAppropriate);

  /**
   * @return the current user, or null if no user is logged in
   */
  public UserIndex getCurrentUserAsUserIndex();

  /**
   * @return an anonymous, temporary user account that can be used as a
   *         placholder if no user is logged in. Never returns null.
   */
  public UserBean getAnonymousUser();

  /**
   * See {@link StandardAuthoritiesService} for definition of anonymous
   * 
   * @return true if the current user is anonymous or if there is no current
   *         user
   */
  public boolean isCurrentUserAnonymous();

  /**
   * See {@link StandardAuthoritiesService} for definition of admin
   * 
   * @return true if the current user is an admin
   */
  public boolean isCurrentUserAdmin();
  
  /**
   * See {@link StandardAuthoritiesService} for definition of reporting
   * 
   * @return true if the current user is a reporting
   */
  public boolean isCurrentUserReporting();

  /**
   * A generic method that dispatches based on the "mode" parameter. If mode is
   * {@link #MODE_LOGIN}, then we pass off to
   * {@link #handleLogin(String, String, String, boolean, boolean)} with
   * registerIfNewUser set to true. If mode is {@link #MODE_REGISTRATION}, then
   * we pass off to {@link #handleRegistration(String, String, String, boolean)}
   * . Finally, if mode is {@link #MODE_ADD_ACCOUNT}, we pass off to
   * {@link #handleAddAccount(String, String, String, boolean)}.
   * 
   * @param type the {@link UserIndexKey} type
   * @param id the {@link UserIndexKey} id
   * @param credentials {@link UserIndex} credentials
   * @param isAnonymous see {@link StandardAuthoritiesService} for definition of
   *          anonymous
   * @param mode one of {@link #MODE_LOGIN}, {@link #MODE_REGISTRATION}, or
   *          {@link #MODE_ADD_ACCOUNT}
   * @return the details of the logged in user on success, otherwise null
   */
  public IndexedUserDetails handleUserAction(String type, String id,
      String credentials, boolean isAnonymous, String mode);

  /**
   * Handle login action for a user with the specified user index
   * type+id+credentials. Supports creating a new user with the specified user
   * index if the registerIfNewUser flag is true. If an existing anonymous user
   * account is already logged in and a new user is created, the existing user
   * account will be migrated to the new user account.
   * 
   * @param type the {@link UserIndexKey} type
   * @param id the {@link UserIndexKey} id
   * @param credentials {@link UserIndex} credentials
   * @param isAnonymous see {@link StandardAuthoritiesService} for definition of
   *          anonymous
   * @param registerIfNewUser if true, automatically register a new user if one
   *          does not exist already
   * @return the details of the logged in user on success, otherwise null
   */
  public IndexedUserDetails handleLogin(String type, String id,
      String credentials, boolean isAnonymous, boolean registerIfNewUser);

  /**
   * Handle registration/user-creation action for a user with the specified user
   * index type+id+credentials. If a user already existed with the specified
   * user index already exists, it will be used. If an existing anonymous user
   * account is already logged in, the existing user account will be migrated to
   * the new user account.
   * 
   * @param type the {@link UserIndexKey} type
   * @param id the {@link UserIndexKey} id
   * @param credentials {@link UserIndex} credentials
   * @param isAnonymous see {@link StandardAuthoritiesService} for definition of
   *          anonymous
   * @return the details of the logged in user on success, otherwise null
   */
  public IndexedUserDetails handleRegistration(String type, String id,
      String credentials, boolean isAnonymous);

  /**
   * Handle the addition of a user index with the specified index
   * type+id+credentials to the currently logged in user. If there is no
   * currently logged in user, a new user will be created with the specified
   * user index.
   * 
   * @param type the {@link UserIndexKey} type
   * @param id the {@link UserIndexKey} id
   * @param credentials {@link UserIndex} credentials
   * @param isAnonymous see {@link StandardAuthoritiesService} for definition of
   *          anonymous
   * @return the details of the logged in user on success, otherwise null
   */
  public IndexedUserDetails handleAddAccount(String type, String id,
      String credentials, boolean isAnonymous);

  /**
   * @param rememberUserPreferencesEnabled true if preferences should be
   *          remembered for the current user
   */
  public void setRememberUserPreferencesEnabled(
      boolean rememberUserPreferencesEnabled);

  /**
   * Set the default search location for the current user
   * 
   * @param locationName
   * @param lat
   * @param lon
   */
  public void setDefaultLocation(String locationName, double lat, double lon);

  /**
   * Clear the default search location for the current user
   */
  public void clearDefaultLocation();

  /**
   * Set the last selected stop ids for the specified user
   * 
   * @param stopIds
   */
  public void setLastSelectedStopIds(List<String> stopIds);

  /**
   * Add a stop bookmark with the specified name, stop ids, and route filter.
   * See {@link UserBean#getBookmarks()}.
   * 
   * @param name
   * @param stopIds
   * @param filter
   * @return the newly created bookmark id
   */
  public int addStopBookmark(String name, List<String> stopIds,
      RouteFilter filter);

  /**
   * Updated a stop bookmark with the specified id with the specified name, stop
   * ids, and route filter. See {@link UserBean#getBookmarks()}.
   * 
   * @param id
   * @param name
   * @param stopIds
   * @param routeFilter
   */
  public void updateStopBookmark(int id, String name, List<String> stopIds,
      RouteFilter routeFilter);

  /**
   * Delete the stop bookmark with the specified id. See
   * {@link UserBean#getBookmarks()}.
   * 
   * @param id
   */
  public void deleteStopBookmarks(int id);

  /**
   * See {@link UserService#registerPhoneNumber(User, String)}
   * 
   * @param phoneNumber the phone number to register to the current user
   * @return the registration code that must be used validate the phoneNumber in
   *         a subsequent call to
   *         {@link #completePhoneNumberRegistration(String)}.
   */
  public String registerPhoneNumber(String phoneNumber);

  /**
   * See {@link UserService#hasPhoneNumberRegistration(UserIndexKey)}
   * 
   * @return if the current user has a pending phone number registration
   *         outstanding
   */
  public boolean hasPhoneNumberRegistration();

  /**
   * Registers the specified phone number with the user by attaching a new
   * UserIndex to the user with the phone number, or merging an existing user
   * account with an existing UserIndex. See
   * {@link UserService#completePhoneNumberRegistration(UserIndex, String)}
   * 
   * @param registrationCode
   * @return true if the registration was successful, otherwise false
   */
  public boolean completePhoneNumberRegistration(String registrationCode);

  /**
   * Clear any pending phone number registration for the current user. See
   * {@link UserService#clearPhoneNumberRegistration(UserIndexKey)}
   */
  public void clearPhoneNumberRegistration();

  /**
   * Mark the specified service alert as read or unread at the specified time
   * for the current user.
   * 
   * @param situationId the service alert situation id
   * @param time the time the service alert was read or marked unread
   * @param isRead whether the service alert should be marked read or unread
   */
  public void markServiceAlertAsRead(String situationId, long time,
      boolean isRead);

  /**
   * Remove the {@link UserIndex} with the specified key from the user. See
   * {@link UserService#removeUserIndexForUser(User, UserIndexKey)}
   * 
   * @param key
   */
  public void removeUserIndex(UserIndexKey key);

  /**
   * Delete the current user. See {@link UserService#deleteUser(User)}
   */
  public void deleteCurrentUser();

  /**
   * Reset the properties to default values for the current user. See
   * {@link UserService#resetUser(User)}.
   */
  public void resetCurrentUser();

  /**
   * Enable admin role for the current user if there are no other admins in the
   * system already. See
   * {@link UserService#enableAdminRoleForUser(User, boolean)}.
   */
  public void enableAdminRole();


}
