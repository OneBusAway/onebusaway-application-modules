package org.onebusaway.integration.phone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.onebusaway.integration.TestSupport;

public class ArrivalsForStopNumberTest extends PhoneTestSupport {

  @Test
  public void test() {

    assertTrue(waitForText("To enter a stop number", 20));
    sendResponse("1");

    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    assertEquals(
        sayAlpha("We need some information about your current location to better serve your request."),
        getReplyAsText());

    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    assertEquals(
        sayAlpha("Please enter a zip code near your current location, followed by the pound sign."),
        getReplyAsText(false));
    sendLongResponse("98105#");

    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    assertEquals(sayAlpha("Your default search location has been set."),
        getReplyAsText());

    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    assertEquals(
        sayAlpha("Please enter your stop number, followed by the pound sign."),
        getReplyAsText(false));
    sendLongResponse("13721#");

    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    int hourOfDay = TestSupport.getHourOfDay();
    boolean checkArrivals = 5 <= hourOfDay && hourOfDay < 24;

    // Make sure it actually repeats
    for (int i = 0; i < 2; i++) {
      if (checkArrivals) {
        // There should be at least one arrival for the 15
        assertEquals(sayAlpha("route"), getReplyAsText());
        assertEquals(sayAlpha("15"), getReplyAsText());
        assertEquals(sayAlpha("to"), getReplyAsText());
        assertEquals(sayAlpha("Downtown Seattle"), getReplyAsText());
        String value = getReplyAsText();
        assertTrue("checking arrival string: " + value,
            isValidArrialString(value));
      }

      // But there may be more
      String next = null;

      while (true) {
        next = getReplyAsText();
        if (next.equals(sayAlpha("route"))) {
          assertEquals(sayAlpha("15"), getReplyAsText());
          assertEquals(sayAlpha("to"), getReplyAsText());
          assertEquals(sayAlpha("Downtown Seattle"), getReplyAsText());
          String value = getReplyAsText();
          assertTrue("checking arrival string: " + value,
              isValidArrialString(value));
        } else {
          break;
        }
      }

      assertEquals(
          sayAlpha("Arrival information is based on the last known location of the bus. Accuracy may vary a few minutes due to traffic conditions."),
          next);
      assertEquals(sayAlpha("Route and arrival data provided by"),
          getReplyAsText());
      assertEquals(sayAlpha("Metro Transit"), getReplyAsText());
      assertEquals(sayAlpha(","), getReplyAsText());

      assertEquals(
          sayAlpha("For more info on a specific route, please press 1, followed by the route number, and then the pound sign."),
          getReplyAsText());

      assertEquals(sayAlpha("To bookmark this location, please press two."),
          getReplyAsText());
      assertEquals(sayAlpha("To return to the main menu, please press three."),
          getReplyAsText());
      assertEquals(
          sayAlpha("If you wish to return to the previous menu, please press star."),
          getReplyAsText());

      assertEquals(sayAlpha("to repeat"), getReplyAsText());
    }
  }

  public static boolean isValidArrialString(String value) {
    Pattern p = Pattern.compile("^SAY ALPHA \"(.*)\" \"0123456789#\\*\"$");
    Matcher m = p.matcher(value);
    if (!m.matches())
      return false;
    value = m.group(1);
    if (value.matches("arriving in \\d+ minutes"))
      return true;
    if (value.matches("arriving in less than one minute"))
      return true;
    if (value.matches("departed \\d+ minutes ago"))
      return true;
    if (value.matches("scheduled in \\d+ minutes"))
      return true;
    if (value.matches("scheduled in less than one minute"))
      return true;
    return false;
  }
}
