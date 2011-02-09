package org.onebusaway.users.services;

public interface ApiKeyPermissionService {
  /**
   * Checks whether a user has permission to access a given service,
   * and marks it as having been used.
   * @return true if the specified key is allowed to access the ggiven service
   */
  public boolean getPermission(String key, String service);

}
