package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.beanutils.converters.DoubleArrayConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onebusaway.users.impl.authentication.DefaultUserAuthenticationToken;
import org.onebusaway.users.model.User;
import org.onebusaway.users.model.UserIndex;
import org.onebusaway.users.model.UserIndexKey;
import org.onebusaway.users.services.StandardAuthoritiesService;
import org.onebusaway.users.services.UserIndexTypes;
import org.onebusaway.users.services.internal.UserIndexRegistrationService;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

public class CurrentUserServiceImplTest {

  private StandardAuthoritiesService _authorities = new StandardAuthoritiesServiceImpl();

  @Before
  public void setup() {

  }

  @After
  public void tearDown() {
    SecurityContext context = SecurityContextHolder.getContext();
    context.setAuthentication(null);
  }

  @Test
  public void testRegisterPhoneNumber() {

    CurrentUserServiceImpl service = new CurrentUserServiceImpl();

    User user = new User();
    user.setId(1234);

    setCurrentUser(UserIndexTypes.USERNAME, "test", user);

    double total = 0;
    int n = 100;
    
    for (int i = 0; i < n; i++) {

      UserIndexRegistrationService registration = Mockito.mock(UserIndexRegistrationService.class);
      service.setUserIndexRegistrationService(registration);

      String code = service.registerPhoneNumber("+12065551234");

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
    assertEquals(5500,mu,1000);
  }

  private void setCurrentUser(String indexType, String indexValue, User user) {

    UserIndex index = new UserIndex();
    index.setId(new UserIndexKey(indexType, indexValue));
    index.setUser(user);
    index.setCredentials("");

    SecurityContext context = SecurityContextHolder.getContext();
    IndexedUserDetailsImpl details = new IndexedUserDetailsImpl(_authorities,
        index);

    DefaultUserAuthenticationToken token = new DefaultUserAuthenticationToken(
        details);
    context.setAuthentication(token);
  }
}
