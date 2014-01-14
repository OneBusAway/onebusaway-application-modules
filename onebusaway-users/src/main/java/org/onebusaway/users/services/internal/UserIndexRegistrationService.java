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
package org.onebusaway.users.services.internal;

import org.onebusaway.users.model.UserIndexKey;

public interface UserIndexRegistrationService {

  public void setRegistrationForUserIndexKey(UserIndexKey key, int userId,
      String registrationCode);
  
  public boolean hasRegistrationForUserIndexKey(UserIndexKey userIndexKey);

  public UserRegistration getRegistrationForUserIndexKey(UserIndexKey key);

  public void clearRegistrationForUserIndexKey(UserIndexKey key);
}
