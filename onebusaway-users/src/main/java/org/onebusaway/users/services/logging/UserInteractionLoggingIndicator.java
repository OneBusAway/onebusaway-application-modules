package org.onebusaway.users.services.logging;

import java.util.Map;

import org.onebusaway.users.model.IndexedUserDetails;

public interface UserInteractionLoggingIndicator {

  /**
   * @param details the target user details entry
   * @return null if logging is not enabled for this user, otherwise a Map of
   *         details to be used as the base of the logging entry
   */
  public Map<String, Object> isLoggingEnabledForUser(IndexedUserDetails details);
}
