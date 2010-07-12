package org.onebusaway.integration.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StopTest extends WebTestSupport {

  @Test
  public void test() {
    open("/where/standard/stop.action?id=1_13721");

    assertTrue(isTextPresent("15th Ave NW & NW Market St"));

    /**
     * Stop Header
     */
    assertTrue(isElementPresent("xpath=//div[@class='arrivalsStopInfo']"));
    assertEquals(
        "15th Ave NW & NW Market St",
        getText("xpath=//div[@class='arrivalsStopInfo']/div/div[@class='arrivalsStopAddress']/a"));
    assertEquals(
        "Stop # 13721 - S bound",
        getText("xpath=//div[@class='arrivalsStopInfo']/div/div[@class='arrivalsStopNumber']/a"));

    /**
     * Nearby Stops
     */
    assertTrue(isElementPresent("xpath=//div[@class='arrivalsNearbyStops']"));
    // Note we can't use the full url here because Tomcat inserts a random
    // SESSION in the url
    String path = "xpath=//div[@class='arrivalsNearbyStops']/div/a[contains(@href,'id=1_14230')]";
    assertEquals("15th Ave NW & NW Market St - N bound", getText(path));

    /**
     * Disclaimer
     */
    assertTrue(isElementPresent("xpath=//div[@class='agenciesSection']"));
    assertTrue(isTextPresent("Schedule and arrival data provided by Metro Transit"));
    assertTrue(isElementPresent("xpath=//div[@class='agenciesSection']/div[@class='agencyDisclaimers']"));
    assertTrue(isTextPresent("A word from from the lawyers at Metro Transit:"));
    assertTrue(isTextPresent("Transit scheduling, geographic, and real-time data provided by permission of King County"));
  }
}
