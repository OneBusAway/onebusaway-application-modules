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
package org.onebusaway.phone.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LocationPronunciationTest {

  @Test
  public void test() {
    LocationPronunciation pro = new LocationPronunciation();
    String result = pro.modify("seattle, wa 98105");
    assertEquals("seattle, wa 9 8 1 0 5", result);
  }
}
