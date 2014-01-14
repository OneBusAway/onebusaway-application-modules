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
package org.onebusaway.federations.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.onebusaway.exceptions.InvalidArgumentServiceException;

import org.junit.Test;

public class AgencyIdSupportTest {
  @Test
  public void test() throws InvalidArgumentServiceException {

    assertEquals("a", AgencyIdSupport.getAgencyIdFromEntityId("a_b"));
    assertEquals("b", AgencyIdSupport.getAgencyIdFromEntityId("b_c_d"));

    try {
      AgencyIdSupport.getAgencyIdFromEntityId("ab");
      fail();
    } catch (InvalidArgumentServiceException ex) {

    }
  }
}
