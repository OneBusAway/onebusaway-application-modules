package org.onebusaway.users.services;

import org.onebusaway.users.model.User;

public interface ApiKeyPermissionService {
  /**
   * Checks whether a user has permission to access a given service.
   */
  public boolean getPermission(User user, String service);

  /**
   * Records a user's use of a given service for accounting purposes.
   * Call this after a user uses a service.
   */
  public void usedKey(User user, String service);
}
