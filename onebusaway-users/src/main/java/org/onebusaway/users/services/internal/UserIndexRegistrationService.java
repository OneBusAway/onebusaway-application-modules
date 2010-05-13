package org.onebusaway.users.services.internal;

import org.onebusaway.users.model.UserIndexKey;

public interface UserIndexRegistrationService {

  public void setRegistrationForUserIndexKey(UserIndexKey key, int userId,
      String registrationCode);

  public UserRegistration getRegistrationForUserIndexKey(UserIndexKey key);

  public void clearRegistrationForUserIndexKey(UserIndexKey key);
}
