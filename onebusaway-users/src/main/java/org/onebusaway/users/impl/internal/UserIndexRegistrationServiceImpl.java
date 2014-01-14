/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
