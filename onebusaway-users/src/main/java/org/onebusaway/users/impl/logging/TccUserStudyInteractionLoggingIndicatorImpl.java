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
package org.onebusaway.users.impl.logging;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.cache.CacheableArgument;
import org.onebusaway.users.model.IndexedUserDetails;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.UserService;
import org.onebusaway.users.services.logging.UserInteractionLoggingIndicator;
import org.springframework.beans.factory.annotation.Autowired;

public class TccUserStudyInteractionLoggingIndicatorImpl implements
    UserInteractionLoggingIndicator {

  private UserService _userService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  @Cacheable
  @Override
  public Map<String, Object> isLoggingEnabledForUser(
      @CacheableArgument(keyProperty = "userIndexKey") IndexedUserDetails details) {
    
    UserIndexKey key = details.getUserIndexKey();
    UserIndex userIndex = _userService.getUserIndexForId(key);
    if (userIndex == null)
      return null;
    User user = userIndex.getUser();
    for (UserIndex index : user.getUserIndices()) {
      UserIndexKey id = index.getId();
      if (id.getType().equals("tccStudyId")) {
        Map<String, Object> entry = new HashMap<String, Object>();
        entry.put("tccStudyId", id.getValue());
        entry.put("credentials", index.getCredentials());
        return entry;
      }
    }
    return null;
  }
}
