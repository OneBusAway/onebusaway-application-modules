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

import org.onebusaway.webapp.gwt.common.control.FlexibleDateParser.DateParseException;

import com.google.gwt.junit.client.GWTTestCase;

public class GWTTestFlexibleDateParser extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.onebusaway.common.web.common.CommonLibrary";
  }

  public void testGetMinutesSinceMidnightAMAndPM() throws DateParseException {

    String[] t6pm = {
        "6 pm", "6 p.m.", "6 p.m", "6 pm.", "6 PM", "6 P.M.", "6 P.M", "6 PM.", "6 Pm", "6 P.m.", "6 P.m", "6 Pm.",
        "6 pM", "6 p.M.", "6 p.M", "6 pM.", "6p", "6p.", "6P", "6P.", "6pm", "6p.m.", "6p.m", "6pm.", "6PM", "6P.M.",
        "6P.M", "6PM.", "6Pm", "6P.m.", "6P.m", "6Pm.", "6pM", "6p.M.", "6p.M", "6pM.", "6p", "6p.", "6P", "6P."};

    assertTimesEquals(18 * 60, t6pm);

    String[] t6am = {
        "6 am", "6 a.m.", "6 a.m", "6 am.", "6 AM", "6 A.M.", "6 A.M", "6 AM.", "6 Am", "6 A.m.", "6 A.m", "6 Am.",
        "6 aM", "6 a.M.", "6 a.M", "6 aM.", "6 a", "6 A", "6 a.", "6 A.", "6am", "6a.m.", "6a.m", "6am.", "6AM",
        "6A.M.", "6A.M", "6AM.", "6Am", "6A.m.", "6A.m", "6Am.", "6aM", "6a.M.", "6a.M", "6aM.", "6a", "6A", "6a.",
        "6A."};

    assertTimesEquals(6 * 60, t6am);
  }

  public void test24HourTime() throws DateParseException {

    assertTimeEquals(5 * 60, "5:00");
    assertTimeEquals(5 * 60, "5");
    assertTimeEquals(5 * 60, "500");
    assertTimeEquals(5 * 60 + 23, "523");
    assertTimeEquals(5 * 60, "0500");
    assertTimeEquals(5 * 60 + 23, "0523");

    assertTimeEquals(15 * 60, "15:00");
    assertTimeEquals(15 * 60, "15");
    assertTimeEquals(15 * 60, "1500");
    assertTimeEquals(15 * 60 + 23, "1523");
  }

  public void testAssortedTimes() throws DateParseException {
    assertTimeEquals(7 * 60 + 23, "7:23");
    assertTimeEquals(7 * 60 + 23, "07:23");
    assertTimeEquals(7 * 60 + 23, "07:23 am");
    assertTimeEquals(7 * 60 + 23, "7:23 am");
    assertTimeEquals(19 * 60 + 23, "07:23 pm");
    assertTimeEquals(19 * 60 + 23, "7:23 pm");
  }

  private void assertTimesEquals(int time, String[] values) throws DateParseException {
    for (String value : values)
      assertTimeEquals(time, value);
  }

  private void assertTimeEquals(int time, String value) throws DateParseException {
    FlexibleDateParser parser = new FlexibleDateParser();
    assertEquals(time, parser.getMintuesSinceMidnight(value));
  }
}
