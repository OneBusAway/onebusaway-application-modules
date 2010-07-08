package org.onebusaway.users.services;

import java.util.List;

import org.onebusaway.users.client.model.UserBean;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;

public interface UserService {
  
  public int getNumberOfUsers();
  
  public List<Integer> getAllUserIds();

  public List<Integer> getAllUsersIds(int offset, int limit);

  public User getUserForId(int userId);

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

  public UserIndex getUserIndexForId(UserIndexKey key);

  public UserIndex getOrCreateUserForIndexKey(UserIndexKey key,
      String credentials, boolean isAnonymous);

  public UserIndex addUserIndexToUser(User user, UserIndexKey key,
      String credentials);

  public void removeUserIndexForUser(User user, UserIndexKey key);
  
  public void setCredentialsForUserIndex(UserIndex userIndex, String credentials);

  public UserBean getUserAsBean(User user);
  
  public void deleteUser(User user);

  public void resetUser(User user);

  public boolean isAnonymous(User user);

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

  public void mergeUsers(User sourceUser, User targetUser);

  public void startUserPropertiesMigration();

  public UserPropertiesMigrationStatus getUserPropertiesMigrationStatus();

  public String registerPhoneNumber(User user, String phoneNumber);

  public boolean hasPhoneNumberRegistration(UserIndexKey userIndexKey);

  public UserIndex completePhoneNumberRegistration(UserIndex userIndex,
      String registrationCode);

  public void clearPhoneNumberRegistration(UserIndexKey userIndexKey);

  public <T> T getAdditionalPropertyForUser(User user, String propertyName);

  public void setAdditionalPropertyForUser(User user, String propertyName,
      Object propertyValue);

}
