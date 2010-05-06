package org.onebusaway.api.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.onebusaway.api.impl.ApiKeyValidationProviderV1Impl;

import org.junit.Before;
import org.junit.Test;

public class ApiKeyValidationProviderV1ImplTest {

  private static final String SALT = "the_salt";

  private ApiKeyValidationProviderV1Impl _service = new ApiKeyValidationProviderV1Impl();

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
