package org.onebusaway.users.services;

public interface ApiKeyPermissionService {
  /**
   * Checks whether a user has permission to access a given service,
   * and marks it as having been used.
   */
  public boolean getPermission(String key, String service);

}
