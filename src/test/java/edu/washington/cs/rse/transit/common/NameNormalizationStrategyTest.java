/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NameNormalizationStrategyTest {

  @Test
  public void testGetNormalizedName() {

    NameNormalizationStrategy strategy = new NameNormalizationStrategy();

    assertEquals("south jackson street",
        strategy.getNormalizedName("S Jackson St"));
    assertEquals("north jackson street",
        strategy.getNormalizedName("N Jackson St"));
    assertEquals("east jackson street",
        strategy.getNormalizedName("E Jackson St"));
    assertEquals("west jackson street",
        strategy.getNormalizedName("W Jackson St"));

    assertEquals("west jackson street west bound",
        strategy.getNormalizedName("W Jackson St W\\B"));
    assertEquals("west jackson street west bound",
        strategy.getNormalizedName("W Jackson St W/B"));
    assertEquals("west jackson street west bound",
        strategy.getNormalizedName("W Jackson St WB"));

  }

}
