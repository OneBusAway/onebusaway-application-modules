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
package org.onebusaway.enterprise.webapp.api;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.onebusaway.enterprise.webapp.api.LoadApiKeysOnInitAction.*;

/**
 * test various combinations of API key data entry.
 */
public class LoadApiKeysOnInitActionTest {

  String R1 = "aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa,,,,,0";
  String R2 = "\"aaaaaaaa-1111-1111-1111-aaaaaaaaaaa1, aaaaaaaa-1111-1111-1111-aaaaaaaaaaa2\",,,,,0";
  String R3 = ",,,,,";
  String R4 = ", ,,,,";
  String R5 = "aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa,First Last,Com Pany,user@example.com,key for Trip Planner Maps,0";
  String R6 = "aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa,First Last,\"Three Word Company, LLC\",user@example.com,,0";
  String R7 = "\"aaaaaaaa-1111-1111-1111-aaaaaaaaaaa1, aaaaaaaa-1111-1111-1111-aaaaaaaaaaa2\",First Last,,user@example.com,,0";
  String R8 = "795d483d-514e-4eac-8acc-0cb86754363f,Drew Dara-Abrams,Interline Technologies,drew@interline.io,\"Interline's Transitland platform\",100";
  @Test
  public void testGetColumn() {
    LoadApiKeysOnInitAction action = new LoadApiKeysOnInitAction();
    List<String> values = action.getColumn(R1, 0, true);
    assertNotNull(values);
    assertEquals(1, values.size());
    assertEquals("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa", values.get(0));

    values = action.getColumn(R2, 0, true);
    assertNotNull(values);
    assertEquals(2, values.size());
    assertEquals("aaaaaaaa-1111-1111-1111-aaaaaaaaaaa1", values.get(0));
    assertEquals("aaaaaaaa-1111-1111-1111-aaaaaaaaaaa2", values.get(1));

    values = action.getColumn(R3, 0, true);
    assertNotNull(values);
    assertEquals(0, values.size());

    values = action.getColumn(R4, 0, true);
    assertNotNull(values);
    assertEquals(0, values.size());

    compareApiKey(action, R5, "aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa", "First Last", "Com Pany", "user@example.com", "key for Trip Planner Maps", "0");
    compareApiKey(action, R6, "aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa", "First Last", "Three Word Company, LLC", "user@example.com", null, "0");
    compareApiKey(action, R7, "aaaaaaaa-1111-1111-1111-aaaaaaaaaaa1", "First Last", null, "user@example.com", null, "0");
    compareApiKey(action, R8, "795d483d-514e-4eac-8acc-0cb86754363f", "Drew Dara-Abrams", "Interline Technologies", "drew@interline.io", "Interline's Transitland platform", "100");
  }

  private void compareApiKey(LoadApiKeysOnInitAction action, String line, String key, String name, String company, String email, String details, String limit) {
//    assertEquals(key, action.getColumn(line, KEY, true).get(0));
//    assertEquals(name, action.getSafeColumn(line, NAME));
//    assertEquals(company, action.getSafeColumn(line, COMPANY));
//    assertEquals(email, action.getSafeColumn(line, EMAIL));
//    assertEquals(details, action.getSafeColumn(line, DETAILS));
    assertEquals(limit, action.getSafeColumn(line, LIMIT));
  }
}