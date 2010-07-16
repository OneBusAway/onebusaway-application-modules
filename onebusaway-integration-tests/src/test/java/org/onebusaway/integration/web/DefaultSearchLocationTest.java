package org.onebusaway.integration.web;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DefaultSearchLocationTest extends WebTestSupport {

  /**
   * Accessing arrival info for a stop should set your default search location
   */
  @Test
  public void testFromStop() {

    open("/logout.action");

    open("/user/index.action");
    assertTrue(isTextPresent("Your Default Search Location:Not Set"));

    open("/where/standard/stop.action?id=1_13721");

    open("/user/index.action");
    assertTrue(isTextPresent("Your Default Search Location:15th Ave NW & NW Market St"));
  }

  /**
   * Entering your location on startup also works
   */
  @Test
  public void testFromZipCodeEntry() throws InterruptedException {

    open("/logout.action");

    open("/user/index.action");
    assertTrue(isTextPresent("Your Default Search Location:Not Set"));

    open("/where/standard/");

    // Wait for the AJAX popup
    waitForCondition(
        "selenium.isTextPresent(\"It looks like this is your first time using OneBusAway.\")",
        "10000");

    assertTrue(isTextPresent("It looks like this is your first time using OneBusAway."));

    type("location", "98107");
    click("setLocationButton");

    // Wait for the AJAX popup
    waitForCondition(
        "selenium.isTextPresent(\"Seattle, WA 98107, USA\")",
        "10000");

    open("/user/index.action");
    assertTrue(isTextPresent("Your Default Search Location:Seattle, WA 98107, USA"));
  }

  /**
   * Finally, you can adjust your personal settings directly
   */
  @Test
  public void testFromDirectChange() throws InterruptedException {

    open("/logout.action");

    open("/user/index.action");
    assertTrue(isTextPresent("Your Default Search Location:Not Set"));

    click("link=Change your default search location");
    type("//input[@type='text']", "98107");
    click("//button[@type='submit']");
    waitForPageToLoad("30000");

    assertTrue(isTextPresent("Your Default Search Location:98107"));
  }
}
