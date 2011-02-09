package org.onebusaway.users.services.logging;

public interface UserInteractionLoggingOutlet {
  public void logInteraction(String serialized);
}
