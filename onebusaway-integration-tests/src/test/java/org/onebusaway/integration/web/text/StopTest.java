package org.onebusaway.integration.web.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onebusaway.integration.TestSupport;
import org.onebusaway.integration.web.WebTestSupport;

public class StopTest extends WebTestSupport {

  @Test
  public void test() {

    open("/where/text/stop.action?id=1_13721");

    assertTrue(isTextPresent("15th Ave NW & NW Market St"));
    assertTrue(isTextPresent("Stop # 13721 - S bound"));

    if (TestSupport.checkArrivalsForRoute15()) {

      assertTrue(isTextPresent("Downtown Seattle"));
      String routeName = getText("xpath=//table[@class='arrivalsTable']/tbody/tr[2]/td[@class='arrivalsRouteEntry']/a");
      if (routeName.contains("E"))
        assertEquals("15E", routeName);
      else
        assertEquals("15", routeName);
    }

    assertTrue(isTextPresent("Nearby stops:"));
    assertEquals("15th Ave NW & NW Market St - N bound",
        getText("link=15th Ave NW & NW Market St - N bound"));

    assertTrue(isTextPresent("Schedule and arrival data provided by Metro Transit"));
    assertTrue(isTextPresent("A word from from the lawyers at Metro Transit:"));
    assertTrue(isTextPresent("Transit scheduling, geographic, and real-time data provided by permission of King County"));
  }
}
