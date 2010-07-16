package org.onebusaway.integration.web.iphone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onebusaway.integration.web.WebTestSupport;

public class SearchByRouteTest extends WebTestSupport {

  @Test
  public void test() {

    open("/where/iphone/");
    type("query", "15");
    click("routes_submit");
    waitForPageToLoad("30000");

    type("location", "98107");
    click("submit");
    waitForPageToLoad("30000");
    
    assertTrue(isTextPresent("15 - Blue Ridge/Downtown"));
    assertTrue(isTextPresent("Stops for direction of travel:"));

    assertEquals("Blue Ridge", getText("link=Blue Ridge"));
    assertEquals("Downtown Seattle", getText("link=Downtown Seattle"));
    
    assertTrue(isTextPresent("Stops:"));
    assertEquals(90,getXpathCount("//div[@id='stops_for_route']/ul/li/a"));
    
    assertEquals("1st Ave & Bay St",
        getText("xpath=//div[@id='stops_for_route']/ul/li[1]/a"));
    assertEquals("W Mercer St & 5th Ave W",
        getText("xpath=//div[@id='stops_for_route']/ul/li[90]/a"));

    click("link=Downtown Seattle");
    waitForPageToLoad("30000");

    assertTrue(isTextPresent("15 - Blue Ridge/Downtown"));
    
    assertTrue(isTextPresent("Stops:"));
    assertEquals(51,getXpathCount("//div[@id='stops_for_route']/ul/li/a"));
    
    assertEquals("15th Ave NW & NW 90th St",
        getText("xpath=//div[@id='stops_for_route']/ul/li[1]/a"));
    assertEquals("S Jackson St & 2nd Ave S",
        getText("xpath=//div[@id='stops_for_route']/ul/li[51]/a"));

    click("link=15th Ave NW & NW Market St");
    waitForPageToLoad("30000");

    assertTrue(isTextPresent("15th Ave NW & NW Market St"));
    assertTrue(isTextPresent("Stop # 13721 - S bound"));
  }
}
