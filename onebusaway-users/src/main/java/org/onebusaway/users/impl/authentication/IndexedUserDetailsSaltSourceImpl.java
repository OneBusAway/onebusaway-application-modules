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
package org.onebusaway.users.impl.authentication;

import org.onebusaway.users.model.IndexedUserDetails;
import org.springframework.security.providers.dao.SaltSource;
import org.springframework.security.userdetails.UserDetails;

public class IndexedUserDetailsSaltSourceImpl implements SaltSource {

  @Override
  public Object getSalt(UserDetails user) {
    if (user instanceof IndexedUserDetails) {
      IndexedUserDetails details = (IndexedUserDetails) user;
      return details.getUserIndexKey().getValue();
    }
    return user.getUsername();
  }
}
