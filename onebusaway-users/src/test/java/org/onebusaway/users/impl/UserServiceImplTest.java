package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

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
