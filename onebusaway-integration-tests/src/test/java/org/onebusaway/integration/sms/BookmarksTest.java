package org.onebusaway.integration.sms;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onebusaway.integration.TestSupport;

public class BookmarksTest extends SmsTestSupport {

  @Test
  public void test() throws Exception {

    url("%23reset");
    assertTrue(isTextPresent("Reset successful."));

    url("13721");
    assertTrue(isTextPresent("Please enter your zip code so we can provide results appropriate to your location."));

    url("98105");

    boolean checkArrivals = TestSupport.checkArrivalsForRoute15();
    if (checkArrivals)
      assertTrue(isTextPresent("15 DwntwnSeattle:"));

    url("%23bookmarks add");
    assertTrue(isTextPresent("Your bookmark was added. Text \"onebus #bookmarks\" to see your bookmarks."));

    url("%23bookmarks");
    assertTrue(isTextPresent("1: 15th Ave NW & NW Market St"));
    assertTrue(isTextPresent("Text \"onebus #bookmark num\" to see bookmark."));

    url("13760");
    if (checkArrivals)
      assertTrue(isTextPresent("15 DwntwnSeattle:"));

    url("%23bookmarks add");
    assertTrue(isTextPresent("Your bookmark was added. Text \"onebus #bookmarks\" to see your bookmarks."));

    url("%23bookmarks");
    assertTrue(isTextPresent("1: 15th Ave NW & NW Market St"));
    assertTrue(isTextPresent("2: 15th Ave NW & NW Leary Way"));
    assertTrue(isTextPresent("Text \"onebus #bookmark num\" to see bookmark."));

    url("%23bookmarks delete 1");
    System.out.println(getBodyText());
    assertTrue(isTextPresent("Your bookmark was deleted. Text \"onebus #bookmarks\" to see your bookmarks"));

    url("%23bookmarks");
    assertTrue(isTextPresent("1: 15th Ave NW & NW Leary Way"));
    assertTrue(isTextPresent("Text \"onebus #bookmark num\" to see bookmark."));

    url("%23bookmarks 1");
    if (checkArrivals)
      assertTrue(isTextPresent("15 DwntwnSeattle:"));
  }
}
