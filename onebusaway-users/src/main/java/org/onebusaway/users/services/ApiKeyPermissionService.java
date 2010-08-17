package org.onebusaway.users.services;

public interface ApiKeyPermissionService {
  /**
   * Checks whether a user has permission to access a given service.
   */
  public boolean getPermission(String key, String service);

  /**
   * Records a user's use of a given service for accounting purposes.
   * Call this after a user uses a service.
   */
  public void usedKey(String key, String service);
}
