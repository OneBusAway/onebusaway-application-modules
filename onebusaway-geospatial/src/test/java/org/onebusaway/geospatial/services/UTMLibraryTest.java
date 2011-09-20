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
package org.onebusaway.geospatial.services;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class UTMLibraryTest {

  @Test
  public void testGetUTMZone() throws NumberFormatException, IOException {

    // Boundary Conditions
    assertEquals("1C", UTMLibrary.getUTMZone(-80, -180));
    assertEquals("60C", UTMLibrary.getUTMZone(-80, 180));
    assertEquals("1X", UTMLibrary.getUTMZone(84, -180));
    assertEquals("60X", UTMLibrary.getUTMZone(84, 180));

    // Weird crossover condition
    assertEquals("60W", UTMLibrary.getUTMZone(71.9999, 180));
    assertEquals("60X", UTMLibrary.getUTMZone(72, 180));
  }
}
