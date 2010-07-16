package org.onebusaway.integration.web.text;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onebusaway.integration.web.WebTestSupport;

public class SearchByStopNumberTest extends WebTestSupport {

  @Test
  public void test() {

    open("/where/text/");
    type("code", "13721");
    click("stops_submit");
    waitForPageToLoad("30000");

    type("location", "98107");
    click("submit");
    waitForPageToLoad("30000");

    assertTrue(isTextPresent("15th Ave NW & NW Market St"));
    assertTrue(isTextPresent("Stop # 13721 - S bound"));
  }
}
