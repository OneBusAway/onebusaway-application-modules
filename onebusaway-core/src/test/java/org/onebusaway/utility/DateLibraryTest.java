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
package org.onebusaway.utility;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class DateLibraryTest {

  @Test
  public void test() throws ParseException {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    Date date = format.parse("2010-01-10 11:24:36 PST");
    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    String string = DateLibrary.getTimeAsIso8601String(date, tz);
    assertEquals("2010-01-10T11:24:36-08:00", string);
  }

  @Test
  public void testGetIso8601StringAsTime() throws ParseException {

    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    Date expected = format.parse("2010-01-10 11:24:36 PST");

    TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
    Date actual = DateLibrary.getIso8601StringAsTime(
        "2010-01-10T11:24:36-08:00", tz);

    assertEquals(expected, actual);
  }
}
