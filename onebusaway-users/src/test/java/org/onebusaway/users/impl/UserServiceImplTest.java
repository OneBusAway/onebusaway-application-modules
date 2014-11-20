/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserDao;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.UserPropertiesService;
import org.onebusaway.users.services.internal.UserIndexRegistrationService;
import org.onebusaway.users.services.internal.UserRegistration;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserServiceImplTest {

  private UserServiceImpl _service;

  private UserDao _userDao;

  private StandardAuthoritiesService _authoritiesService = new MockStandardAuthoritiesServiceImpl();

  @Before
  public void setup() {
    _service = new UserServiceImpl();

    _service.setAuthoritiesService(_authoritiesService);

    _userDao = Mockito.mock(UserDao.class);
    _service.setUserDao(_userDao);
  }

  @After
  public void tearDown() {
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(null);
  }

  @Test
  public void testGetNumberOfUsers() {
    Mockito.when(_userDao.getNumberOfUsers()).thenReturn(5);
    assertEquals(5, _service.getNumberOfUsers());
    Mockito.verify(_userDao).getNumberOfUsers();
  }

  @Test
  public void getAllUserIds() {
    List<Integer> ids = Arrays.asList(1, 2, 3);
    Mockito.when(_userDao.getAllUserIds()).thenReturn(ids);
    assertEquals(ids, _service.getAllUserIds());
    Mockito.verify(_userDao).getAllUserIds();
  }

  @Test
  public void getAllUserIdsForRange() {
    List<Integer> ids = Arrays.asList(1, 2, 3);
    Mockito.when(_userDao.getAllUserIdsInRange(0, 3)).thenReturn(ids);
    assertEquals(ids, _service.getAllUserIdsInRange(0, 3));
    Mockito.verify(_userDao).getAllUserIdsInRange(0, 3);
  }

  @Test
  public void testRegisterPhoneNumber() {

    User user = createUser(1234);

    double total = 0;
    int n = 100;

    for (int i = 0; i < n; i++) {

      UserIndexRegistrationService registration = Mockito.mock(UserIndexRegistrationService.class);
      _service.setUserIndexRegistrationService(registration);

      String code = _service.registerPhoneNumber(user, "+12065551234");

      UserIndexKey key = new UserIndexKey(UserIndexTypes.PHONE_NUMBER,
          "12065551234");
      Mockito.verify(registration).setRegistrationForUserIndexKey(key, 1234,
          code);

      int codeAsNumber = Integer.parseInt(code);
      assertTrue(codeAsNumber >= 1000);
      assertTrue(codeAsNumber <= 9999);
      total += codeAsNumber;
    }

    double mu = total / n;
    assertEquals(5500, mu, 1000);
  }

  @Test
  public void testCompletePhoneNumberRegistration() {

    User userA = createUser(1234);

    UserIndexKey key = new UserIndexKey(UserIndexTypes.PHONE_NUMBER,
        "12065551234");

    UserDao userDao = Mockito.mock(UserDao.class);
    _service.setUserDao(userDao);

    Mockito.when(userDao.getUserForId(1234)).thenReturn(userA);

    UserIndex migratedIndex = new UserIndex();
    migratedIndex.setId(key);
    migratedIndex.setUser(userA);
    migratedIndex.setCredentials("");
    Mockito.when(userDao.getUserIndexForId(key)).thenReturn(migratedIndex);

    UserIndexRegistrationService registrationService = Mockito.mock(UserIndexRegistrationService.class);
    _service.setUserIndexRegistrationService(registrationService);

    UserRegistration registration = new UserRegistration(1234, "5555");
    Mockito.when(registrationService.getRegistrationForUserIndexKey(key)).thenReturn(
        registration);

    UserPropertiesService userPropertiesService = Mockito.mock(UserPropertiesService.class);
    _service.setUserPropertiesService(userPropertiesService);

    User userB = createUser(1235);
    UserIndex index = createUserIndex(key.getType(), key.getValue(), userB);

    UserIndex updated = _service.completePhoneNumberRegistration(index, "5554");
    assertTrue(updated == null);

    updated = _service.completePhoneNumberRegistration(index, "5555");
    assertTrue(updated != null);

    Mockito.verify(userPropertiesService).mergeProperties(userB, userA);
  }

  @Test
  public void testGetNumberOfStaleUsers() {
    ArgumentCaptor<Date> captor = ArgumentCaptor.forClass(Date.class);
    Mockito.when(_userDao.getNumberOfStaleUsers(captor.capture())).thenReturn(
        10L);
    assertEquals(10, _service.getNumberOfStaleUsers());
    Date actual = captor.getValue();
    Calendar c = Calendar.getInstance();
    c.add(Calendar.MONTH, -1);
    Date expected = c.getTime();
    assertTrue(Math.abs(expected.getTime() - actual.getTime()) < 30 * 1000);
  }

  @Test
  public void deleteStaleUsers() {
    List<Integer> userIds = Arrays.asList(1, 2, 3);
    Mockito.when(
        _userDao.getStaleUserIdsInRange(Mockito.any(Date.class), Mockito.eq(0),
            Mockito.eq(100))).thenReturn(userIds);
    
    _service.start();
    
    assertFalse(_service.isDeletingStaleUsers());
    _service.deleteStaleUsers();
    assertTrue(_service.isDeletingStaleUsers());
    _service.cancelDeleteStaleUsers();
    assertFalse(_service.isDeletingStaleUsers());
    
    _service.stop();
  }

  private User createUser(int userId) {
    User user = new User();
    user.setId(userId);
    user.setCreationTime(new Date());
    return user;
  }

  private UserIndex createUserIndex(String indexType, String indexValue,
      User user) {

    UserIndex index = new UserIndex();
    index.setId(new UserIndexKey(indexType, indexValue));
    index.setUser(user);
    index.setCredentials("");

    user.getUserIndices().add(index);

    return index;
  }
}
