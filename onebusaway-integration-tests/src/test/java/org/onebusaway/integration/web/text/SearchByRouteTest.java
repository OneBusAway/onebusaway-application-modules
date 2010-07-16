package org.onebusaway.integration.web.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onebusaway.integration.web.WebTestSupport;

public class SearchByRouteTest extends WebTestSupport {

  @Test
  public void test() {

    open("/where/text/");
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
    assertEquals(90,getXpathCount("//div[@id='stops_for_route']/div/a"));
    
    assertEquals("1st Ave & Bay St - SE bound",
        getText("xpath=//div[@id='stops_for_route']/div[2]/a"));
    assertEquals("W Mercer St & 5th Ave W - W bound",
        getText("xpath=//div[@id='stops_for_route']/div[91]/a"));

    click("link=Downtown Seattle");
    waitForPageToLoad("30000");

    assertTrue(isTextPresent("15 - Blue Ridge/Downtown"));
    
    assertTrue(isTextPresent("Stops:"));
    assertEquals(51,getXpathCount("//div[@id='stops_for_route']/div/a"));
    
    assertEquals("15th Ave NW & NW 90th St - N bound",
        getText("xpath=//div[@id='stops_for_route']/div[2]/a"));
    assertEquals("S Jackson St & 2nd Ave S - E bound",
        getText("xpath=//div[@id='stops_for_route']/div[52]/a"));

    click("link=15th Ave NW & NW Market St - S bound");
    waitForPageToLoad("30000");

    assertTrue(isTextPresent("15th Ave NW & NW Market St"));
    assertTrue(isTextPresent("Stop # 13721 - S bound"));
  }
}
