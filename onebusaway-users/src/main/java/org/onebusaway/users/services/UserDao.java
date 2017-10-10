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

import java.util.Date;
import java.util.List;

import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserRole;

public interface UserDao {

  public int getNumberOfUsers();

  public List<Integer> getAllUserIds();

  public List<Integer> getAllUserIdsInRange(int offset, int limit);

  /**
   * Count the number of users whose last access time (see
   * {@link User#getLastAccessTime()}) is greater than the specified last access
   * time.
   * 
   * @param lastAccessTime user accounts that have not been accessed since this
   *          time are considered stale
   * @return the number of stale user accounts
   */
  public long getNumberOfStaleUsers(Date lastAccessTime);

  public List<Integer> getStaleUserIdsInRange(Date lastAccessTime, int offset,
      int limit);

  public User getUserForId(int id);

  public void saveOrUpdateUser(User user);

  public void saveOrUpdateUsers(User... users);

  public void deleteUser(User user);

  public int getNumberOfUserRoles();

  public UserRole getUserRoleForName(String name);

  public void saveOrUpdateUserRole(UserRole userRole);

  public int getNumberOfUsersWithRole(UserRole role);

  /**
   * 
   * @return the set of {@linkplain UserIndexKey#getValue() UserIndexKey values}
   *         having the specified {@linkplain UserIndexKey#getType()
   *         UserIndexKey type}
   */
  public List<String> getUserIndexKeyValuesForKeyType(String keyType);

  /**
   * @return the number (count) of Users of type keyType
   */
  public Integer getUserKeyCount(String keyType);

  /**
   * @return the list of Users of type keyType, for maxResults of users staring with 'start'
   */
  public List<User> getUsersForKeyType(final int start, final int maxResults, final String keyType);

  public UserIndex getUserIndexForId(UserIndexKey key);

  public void saveOrUpdateUserIndex(UserIndex userIndex);

  public void deleteUserIndex(UserIndex index);
}
