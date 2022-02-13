/**
 * Copyright (C) 2021 Cambridge Systematics, Inc.
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
package org.onebusaway.users.impl.authentication;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Confirm that spring security migration remains backwards compatible to
 * existing passwords
 */
public class LegacyPasswordEncoderTest {

  @Test
  public void encodePassword() {
    LegacyPasswordEncoder lpe = new LegacyPasswordEncoder("SHA-256");
    lpe.setEncodeHashAsBase64(true);

    String expectedPassword = "pKiMCHK/ZSu57YA+zl/W6CNUg4qb9Zq0ursdqzIhVOE=";
    assertEquals(expectedPassword, lpe.encodePassword("admin", "admin"));

    String v1Password = "v1|pKiMCHK/ZSu57YA+zl/W6CNUg4qb9Zq0ursdqzIhVOE=";
    VersionedPasswordEncoder vpe = new VersionedPasswordEncoder();
    vpe.setVersionId("v1");
    vpe.setPasswordEncoder(lpe);

    assertEquals(v1Password, vpe.encodePassword("admin", "admin"));

  }
}