package org.onebusaway.users.impl.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SaltedPasswordValidationProviderV1ImplTest {

  @Test
  public void test() {
    SaltedPasswordValidationProviderV1Impl provider = new SaltedPasswordValidationProviderV1Impl();
    String key = provider.generateKey("the_password1");
    assertFalse(provider.isValidKey(key));
    assertFalse(provider.isValidKey(key, "the_password2"));
    assertTrue(provider.isValidKey(key, "the_password1"));
  }
}
