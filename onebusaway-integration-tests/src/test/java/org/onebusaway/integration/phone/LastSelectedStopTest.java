package org.onebusaway.integration.phone;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onebusaway.integration.TestSupport;

public class LastSelectedStopTest extends PhoneTestSupport {

  @Test
  public void test() {

    int hourOfDay = TestSupport.getHourOfDay();
    boolean checkArrivals = 5 <= hourOfDay && hourOfDay < 24;

    assertTrue(waitForText(
        "To check your most recent stop, please press five.", 20));
    sendResponse("5");

    assertTrue(waitForText("You have no previous stop to check.", 10));
    sendDefaultResponse();

    assertTrue(waitForText("To enter a stop number, please press one.", 20));
    sendResponse("1");

    assertTrue(waitForText("Please enter a zip code", 10));
    sendLongResponse("98105#");

    assertTrue(waitForText("Please enter your stop number", 10));
    sendLongResponse("13721#");

    if (checkArrivals) {
      assertTrue(waitForText("route", 10));
      sendDefaultResponse();
    }

    assertTrue(waitForText("To return to the main menu", 200));
    sendResponse("3");

    assertTrue(waitForText(
        "To check your most recent stop, please press five.", 20));
    sendResponse("5");
    
    if (checkArrivals) {
      assertTrue(waitForText("route", 10));
      sendDefaultResponse();
    }
  }
}
