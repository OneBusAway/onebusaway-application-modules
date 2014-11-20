/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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

package org.onebusaway.utility;

import static org.junit.Assert.*;

import org.junit.Test;

public class MavenVersionTest {

  @Test
  public void testWithQualifier() {
    MavenVersion mv = new MavenVersion("1.1.12-SNAPSHOT");

    assertEquals("1", mv.getMajor());
    assertEquals("1", mv.getMinor());
    assertEquals("12", mv.getIncremental());
    assertEquals("SNAPSHOT", mv.getQualifier());
  }

  @Test
  public void testWithoutQualifier() {
    MavenVersion mv = new MavenVersion("1.1.11");

    assertEquals("1", mv.getMajor());
    assertEquals("1", mv.getMinor());
    assertEquals("11", mv.getIncremental());
    assertNull(mv.getQualifier());
  }

}