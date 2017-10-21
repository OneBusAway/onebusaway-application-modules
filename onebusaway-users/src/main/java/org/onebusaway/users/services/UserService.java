/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;

/**
 * Service methods for performing operations on user accounts.
 * 
 * @author bdferris
 * @see UserIndex
 * @see User
 * @see UserBean
 */
public interface UserService {

  /**
   * @return the number of users in the system
   */
  public int getNumberOfUsers();

  /**
   * @return the list of all user ids
   */
  public List<Integer> getAllUserIds();

  /**
   * @param offset index offset into the full user id list
   * @param limit length of subset of the user id list to return
   * @return a subset of the list of all user ids in the system
   */
  public List<Integer> getAllUserIdsInRange(int offset, int limit);

  /**
   * @param userId see {@link User#getId()}
   * @return the user with the specifed id, or null if not found
   */
  public User getUserForId(int userId);

  /**
   * @return the number of users with the admin role set
   */
  public int getNumberOfAdmins();

  /****
   * {@link UserIndex} Methods
   ****/

  /**
   * 
   * @return the set of {@linkplain UserIndexKey#getValue() UserIndexKey values}
   *         having the specified {@linkplain UserIndexKey#getType()
   *         UserIndexKey type}
   */
  public List<String> getUserIndexKeyValuesForKeyType(String keyType);

  /**
   * @return the number (count) of Users of type=API_KEY
   */
  public Integer getApiKeyCount();

  /**
   * @return the list of Users of type=API_KEY for maxResults of users staring with 'start'
   */
  public List<User> getApiKeys(final int start, final int maxResults);

  /**
   * @param key see {@link UserIndex#getId()}
   * @return the user index with the specified key, or null if not found
   */
  public UserIndex getUserIndexForId(UserIndexKey key);

  /**
   * * @return the user index from the username string
   * */
  public UserIndex getUserIndexForUsername(String username);

  /**
   * @param key see {@link UserIndex#getId()}
   * @param credentials {@link UserIndex#getCredentials()}
   * @param isAnonymous is a newly created user anonymous -
   *          {@link User#getRoles()}
   * @return an existing user index with the specified key if it already exists,
   *         or a newly created user index (and underlying user) with the
   *         specified properties
   */
  public UserIndex getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials, boolean isAnonymous);

  /**
   * @param username
   * @param password
   * @return an existing user index with the specified username if it already
   *         exists, or a newly created user index (and underlying user) with
   *         the specified username and password credentials
   */
  public UserIndex getOrCreateUserForUsernameAndPassword(String username,
      String password);

  /**
   * Add a {@link UserIndex} with the specified id and credentials to an
   * existing user, returning the new index. If an index with the specified id
   * already exists, it is returned instead.
   * 
   * @param user the target user
   * @param key see {@link UserIndex#getId()}
   * @param credentials see {@link UserIndex#getCredentials()}
   * @return the newly attached user index, or an existing index if already
   *         attached
   */
  public UserIndex addUserIndexToUser(User user, UserIndexKey key,
      String credentials);

  /**
   * Remove the {@link UserIndex} with the specified id from the user.
   * 
   * @param user
   * @param key see {@link UserIndex#getId()}
   */
  public void removeUserIndexForUser(User user, UserIndexKey key);

  /**
   * Update the credentials for the specified user index
   * 
   * @param userIndex
   * @param credentials
   */
  public void setCredentialsForUserIndex(UserIndex userIndex, String credentials);

  /**
   * Update the password for the {@link UserIndexTypes#USERNAME} user index
   * 
   * @param userIndex
   * @param password
   */
  public void setPasswordForUsernameUserIndex(UserIndex userIndex,
      String password);

  /**
   * @param user
   * @return the specified user as a user bean object
   */
  public UserBean getUserAsBean(User user);

  /**
   * @return an anonymous default user object
   */
  public UserBean getAnonymousUser();

  /**
   * Delete the specified user. Will delete any {@link UserIndex} objects
   * pointing to that user as well.
   * 
   * @param user
   */
  public void deleteUser(User user);

  /**
   * Reset all properties for the specified user to default values.
   * 
   * @param user
   */
  public void resetUser(User user);

  /**
   * Is the specified user anonymous? See the discussion in
   * {@link StandardAuthoritiesService}
   * 
   * @param user
   * @return true if the user is anonymous, otherwise false
   */
  public boolean isAnonymous(User user);

  /**
   * Is the specified user an administrator? See the discussion in
   * {@link StandardAuthoritiesService}
   * 
   * @param user
   * @return true if the user is an administrator, otherwise false
   */
  public boolean isAdministrator(User user);

  /**
   * Enable the admin role for a User. For admin bootstrapping, we have a check
   * that will only allow you to set an admin role if no other admins exist.
   * This would be useful for marking the very first user in a system as admin.
   * 
   * @param user the user to mark as an admin
   * @param onlyIfNoOtherAdmins when true, will only add the admin role if no
   *          other users are marked as admin
   */
  public void enableAdminRoleForUser(User user, boolean onlyIfNoOtherAdmins);

  /**
   * Remove the admin role for a User.
   * 
   * @param user
   * @param onlyIfOtherAdmins when true, will only remove the admin role if at
   *          least one other user is marked as admin
   */
  public void disableAdminRoleForUser(User user, boolean onlyIfOtherAdmins);

  /**
   * Given two user accounts, merge the two users into one. The source user is
   * deleted while the target user is updated. Properties in the target user
   * take presedence over properties in the source user if there is overlap.
   * 
   * @param sourceUser this user will be deleted
   * @param targetUser this user will be updated and kept
   */
  public void mergeUsers(User sourceUser, User targetUser);

  /**
   * Start the user property migration task - see
   * {@link UserPropertiesMigration}
   * 
   */
  public void startUserPropertiesMigration();

  /**
   * See {@link UserPropertiesMigration}
   * 
   * @return the status for the user properties migration task
   */
  public UserPropertiesMigrationStatus getUserPropertiesMigrationStatus();

  /**
   * Begin phone number registration for the specified user. Returns a code that
   * the user must specify in a call to
   * {@link #completePhoneNumberRegistration(UserIndex, String)} to verify that
   * they do in fact own that phone number.
   * 
   * @param user
   * @param phoneNumber
   * @return the code that must be supplied in a subsequent call to
   *         {@link #completePhoneNumberRegistration(UserIndex, String)}
   */
  public String registerPhoneNumber(User user, String phoneNumber);

  /**
   * @param userIndexKey
   * @return true if a phone number registration task is pending for the
   *         specified user
   */
  public boolean hasPhoneNumberRegistration(UserIndexKey userIndexKey);

  /**
   * Complete phone number registration. If the registrationCode matches one
   * returned in a previous call to {@link #registerPhoneNumber(User, String)},
   * then registration is completed by creating a new {@link UserIndex} with
   * type {@link UserIndexTypes#PHONE_NUMBER} with the phone number specified in
   * the previous call to register phone number.
   * 
   * @param userIndex
   * @param registrationCode
   * @return the newly created {@link UserIndex} object for the phone number
   *         user index
   */
  public UserIndex completePhoneNumberRegistration(UserIndex userIndex,
      String registrationCode);

  /**
   * Reset a previous call to {@link #registerPhoneNumber(User, String)} for the
   * specified user
   * 
   * @param userIndexKey
   */
  public void clearPhoneNumberRegistration(UserIndexKey userIndexKey);

  /**
   * @param key an API key
   * @param forceRefresh guarantees that supplied value has not been cached
   * @return the minimum interval between requests in milliseconds for the key,
   *         or null for a key with no permission to access the API
   */
  public Long getMinApiRequestIntervalForKey(String key, boolean forceRefresh);

  /**
   * Deletes stale users from the system. Stale users have a last access time of
   * more than a month ago.
   */
  public void deleteStaleUsers();

  /**
   * @return true if the task to delete stale users is currently running.
   */
  public boolean isDeletingStaleUsers();

  /**
   * Cancel the task to delete state users (started with
   * {@link #deleteStaleUsers()}) if it is running.
   */
  public void cancelDeleteStaleUsers();

  /**
   * @return the number of user accounts that have not been accessed in the last
   *         month
   */
  public long getNumberOfStaleUsers();
}
