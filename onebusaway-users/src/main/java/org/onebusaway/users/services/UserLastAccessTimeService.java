package org.onebusaway.users.services;

public interface UserLastAccessTimeService {
  public void handleAccessForUser(int userId, long accessTime);
}
