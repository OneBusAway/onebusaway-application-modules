package org.onebusaway.users.impl.internal;

import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.internal.UserIndexRegistrationService;
import org.onebusaway.users.services.internal.UserRegistration;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class UserIndexRegistrationServiceImpl implements
    UserIndexRegistrationService {

  private Cache _cache;

  public void setCache(Cache cache) {
    _cache = cache;
  }

  /****
   * {@link UserIndexRegistrationService} Interface
   ****/

  @Override
  public void clearRegistrationForUserIndexKey(UserIndexKey key) {
    _cache.remove(key);
  }
  
  @Override
  public boolean hasRegistrationForUserIndexKey(UserIndexKey userIndexKey) {
    return _cache.get(userIndexKey) != null;
  }

  @Override
  public UserRegistration getRegistrationForUserIndexKey(UserIndexKey key) {
    Element element = _cache.get(key);
    if (element == null)
      return null;
    return (UserRegistration) element.getValue();
  }

  @Override
  public void setRegistrationForUserIndexKey(UserIndexKey key, int userId,
      String registrationCode) {
    Element element = new Element(key, new UserRegistration(userId,
        registrationCode));
    _cache.put(element);
  }


}
