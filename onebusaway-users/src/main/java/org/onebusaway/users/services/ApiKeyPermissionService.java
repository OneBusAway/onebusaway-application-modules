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
package org.onebusaway.users.services;

public interface ApiKeyPermissionService {
  
  public enum Status {
    AUTHORIZED,
    UNAUTHORIZED,
    RATE_EXCEEDED;
  }
  /**
   * Checks whether a user has permission to access a given service,
   * and marks it as having been used.
   * @return true if the specified key is allowed to access the ggiven service
   */
  public Status getPermission(String key, String service);

}
