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
package org.onebusaway.presentation.impl;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.container.cache.CacheableArgument;
import org.onebusaway.presentation.services.InitialSetupService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InitialSetupServiceImpl implements InitialSetupService {

  private UserService _userService;

  @Autowired
  public void setUserService(UserService userService) {
    _userService = userService;
  }

  /**
   * Since this is going to be called for EVERY incoming web-request, we don't
   * want to do something like make a database hit for each request. Thus, we
   * cache.
   */
  @Cacheable
  @Override
  public boolean isInitialSetupRequired(
      @CacheableArgument(cacheRefreshIndicator = true) boolean forceRefresh) {
    int numberOfAdmins = _userService.getNumberOfAdmins();
    return numberOfAdmins == 0;
  }
}
