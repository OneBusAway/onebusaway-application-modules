package org.onebusaway.users.services.logging;

import java.util.Map;

public interface UserInteractionLoggingService {
  public Map<String, Object> isInteractionLoggedForCurrentUser();
  public void logInteraction(Map<String, Object> entry);
}
