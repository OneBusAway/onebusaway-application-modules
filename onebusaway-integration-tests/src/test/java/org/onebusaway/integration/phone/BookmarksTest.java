package org.onebusaway.integration.phone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onebusaway.integration.TestSupport;

public class BookmarksTest extends PhoneTestSupport {

  @Test
  public void test() {

    assertTrue(waitForText("To enter a stop number, please press one.", 20));
    sendResponse("1");

    assertTrue(waitForText("Please enter a zip code", 10));
    sendLongResponse("98105#");

    assertTrue(waitForText("Please enter your stop number", 10));
    sendLongResponse("13721#");

    assertTrue(waitForText("To bookmark this location", 200));
    sendResponse("2");

    assertTrue(waitForText("This location has been bookmarked", 10));
    sendDefaultResponse();

    assertTrue(waitForText("To return to the main menu", 200));
    sendResponse("3");

    assertTrue(waitForText("To access your bookmarked stops", 20));
    sendResponse("3");

    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    // Make sure it repeats
    for (int i = 0; i < 2; i++) {
      assertEquals(sayAlpha("for"), getReplyAsText());
      assertEquals(sayAlpha("15th avenue north & north Market street"),
          getReplyAsText());
      assertEquals(sayAlpha("please press"), getReplyAsText());
      assertEquals(sayAlpha("1"), getReplyAsText());
      assertEquals(
          sayAlpha("If you wish to return to the previous menu, please press star."),
          getReplyAsText());
      assertEquals(sayAlpha("to repeat"), getReplyAsText());
    }

    assertTrue(waitForText("15th avenue", 5));
    sendResponse("1");

    assertEquals("WAIT FOR DIGIT 500", getReplyAsText());

    boolean checkArrivals = TestSupport.checkArrivalsForRoute15();

    if (checkArrivals) {

      // There should be at least one arrival for the 15
      assertEquals(sayAlpha("route"), getReplyAsText());
      
      String reply = getReplyAsText();
      assertTrue(reply.equals(sayAlpha("15")) || reply.equals(sayAlpha("15 express")));
      
      assertEquals(sayAlpha("to"), getReplyAsText());
      
      reply = getReplyAsText();
      assertTrue(reply.equals(sayAlpha("Downtown Seattle")) || reply.equals(sayAlpha("Downtown Seattle - Express")));
      
      reply = getReplyAsText();
      assertTrue("checking arrival string: " + reply,
          ArrivalsForStopNumberTest.isValidArrialString(reply));
    }

  }
}
