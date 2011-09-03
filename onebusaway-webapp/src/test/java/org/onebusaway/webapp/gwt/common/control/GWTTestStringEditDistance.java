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
package org.onebusaway.webapp.gwt.common.control;

import com.google.gwt.junit.client.GWTTestCase;

public class GWTTestStringEditDistance extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.onebusaway.common.web.common.CommonLibrary";
  }

  public void test01() {
    assertEquals(0, StringEditDistance.getEditDistance("a", "a"));
    assertEquals(1, StringEditDistance.getEditDistance("a", "b"));
    assertEquals(0, StringEditDistance.getEditDistance("aa", "aa"));
    assertEquals(1, StringEditDistance.getEditDistance("aa", "ab"));
    assertEquals(1, StringEditDistance.getEditDistance("aa", "aac"));
    assertEquals(1, StringEditDistance.getEditDistance("ct", "cat"));
  }

}
