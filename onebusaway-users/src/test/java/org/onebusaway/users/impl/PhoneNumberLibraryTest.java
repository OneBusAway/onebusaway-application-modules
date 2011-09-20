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
package org.onebusaway.users.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PhoneNumberLibraryTest {

  @Test
  public void testNormalizePhoneNumber() {

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("12065551234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("+12065551234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("2065551234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("+2065551234"));

    assertEquals("5551234", PhoneNumberLibrary.normalizePhoneNumber("5551234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber(" 1 206 555 1234 "));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("206-555-1234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("206.555.1234"));

    assertEquals("12065551234",
        PhoneNumberLibrary.normalizePhoneNumber("206-555 (1234)"));
  }
  
  @Test
  public void testSegmentPhoneNumber() {
    
    String[] segments = PhoneNumberLibrary.segmentPhoneNumber("12063038220");
    assertEquals(4,segments.length);
    assertEquals("1",segments[0]);
    assertEquals("206",segments[1]);
    assertEquals("303",segments[2]);
    assertEquals("8220",segments[3]);
    
    segments = PhoneNumberLibrary.segmentPhoneNumber("2063038220");
    assertEquals(4,segments.length);
    assertEquals("1",segments[0]);
    assertEquals("206",segments[1]);
    assertEquals("303",segments[2]);
    assertEquals("8220",segments[3]);
    
    segments = PhoneNumberLibrary.segmentPhoneNumber("3038220");
    assertEquals(2,segments.length);
    assertEquals("303",segments[0]);
    assertEquals("8220",segments[1]);
    
    segments = PhoneNumberLibrary.segmentPhoneNumber("038220");
    assertEquals(2,segments.length);
    assertEquals("03",segments[0]);
    assertEquals("8220",segments[1]);
    
    segments = PhoneNumberLibrary.segmentPhoneNumber("8220");
    assertEquals(1,segments.length);
    assertEquals("8220",segments[0]);
  }
}
