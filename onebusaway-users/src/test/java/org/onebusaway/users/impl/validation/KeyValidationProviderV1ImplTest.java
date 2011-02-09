package org.onebusaway.users.impl.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.onebusaway.users.impl.validation.KeyValidationProviderV1Impl;

import org.junit.Before;
import org.junit.Test;

public class KeyValidationProviderV1ImplTest {

  private static final String SALT = "the_salt";

  private KeyValidationProviderV1Impl _service = new KeyValidationProviderV1Impl();

  @Before
  public void prep() {
    _service.setPrivateSalt(SALT);
  }

  @Test
  public void testSignAndVerify() throws Exception {
    String key = _service.generateKey("some.email@gmail.com");
    assertTrue(_service.isValidKey(key));
    assertFalse(_service.isValidKey("X" + key));
  }
}
