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
