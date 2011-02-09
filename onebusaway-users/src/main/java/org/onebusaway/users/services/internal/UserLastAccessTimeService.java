package org.onebusaway.users.services.internal;

public interface UserLastAccessTimeService {
  public void handleAccessForUser(int userId, long accessTime);
}
