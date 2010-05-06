package org.onebusaway.users.services;

import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.model.UserRole;

public interface UserDao {

  public void saveOrUpdateUser(User user);

  public int getNumberOfUserRoles();
  
  public UserRole getUserRoleForName(String name);

  public void saveOrUpdateUserRole(UserRole userRole);

  public UserIndex getUserIndexForId(UserIndexKey key);

  public void saveOrUpdateUserIndex(UserIndex userIndex);
}
