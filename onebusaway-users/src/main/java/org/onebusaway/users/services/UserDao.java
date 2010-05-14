package org.onebusaway.users.services;

import java.util.List;

import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserRole;

public interface UserDao {
  
  public int getNumberOfUsers();
  
  public List<Integer> getAllUsersIds(int offset, int limit);
  
  public User getUserForId(int id);

  public void saveOrUpdateUser(User user);
  
  public void saveOrUpdateUsers(User... users);

  public void deleteUser(User user);

  public int getNumberOfUserRoles();

  public UserRole getUserRoleForName(String name);

  public void saveOrUpdateUserRole(UserRole userRole);

  public int getNumberOfUsersWithRole(UserRole role);

  public UserIndex getUserIndexForId(UserIndexKey key);
}
