/**
 * Copyright (C) 2026 Open Transit Software Foundation
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
package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.ApiKeyPermissionService;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Every API request validates its key. Once a key's permission is cached, that
 * check must not touch the database (issue #482).
 */
@ContextConfiguration(locations = "classpath:org/onebusaway/users/application-context-api-key-caching-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ApiKeyPermissionCachingTest {

  private static final String KEY = "cached-key";

  @Autowired
  private UserService _userService;

  @Autowired
  private UserPropertiesService _userPropertiesService;

  @Autowired
  private ApiKeyPermissionService _keyService;

  @Autowired
  private ConnectionCountingDataSource _dataSource;

  @Before
  public void setup() {
    UserIndexKey indexKey = new UserIndexKey(UserIndexTypes.API_KEY, KEY);
    UserIndex userIndex = _userService.getOrCreateUserForIndexKey(indexKey,
        KEY, false);
    _userPropertiesService.authorizeApi(userIndex.getUser(), 0);
    _userService.getMinApiRequestIntervalForKey(KEY, true);
  }

  @Test
  public void cachedKeyCheckDoesNotBorrowAConnection() {
    _dataSource.reset();

    assertEquals(ApiKeyPermissionService.Status.AUTHORIZED,
        _keyService.getPermission(KEY, "api"));

    assertEquals(0, _dataSource.getConnectionCount());
  }
}
