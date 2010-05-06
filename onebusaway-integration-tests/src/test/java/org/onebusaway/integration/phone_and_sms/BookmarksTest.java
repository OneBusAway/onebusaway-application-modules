package org.onebusaway.integration.phone_and_sms;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.onebusaway.integration.phone.PhoneTestSupport;
import org.onebusaway.integration.sms.SmsTestSupport;

public class BookmarksTest {

  @Test
  public void test() throws IOException {
    
    /**
     * Add a bookmark using the phone interface
     */
    PhoneTestSupport phone = new PhoneTestSupport();
    phone.start();

    assertTrue(phone.waitForText("To enter a stop number, please press one.", 20));
    phone.sendResponse("1");
    
    assertTrue(phone.waitForText("Please enter a zip code",10));
    phone.sendLongResponse("98105#");

    assertTrue(phone.waitForText("Please enter your stop number",10));
    phone.sendLongResponse("13721#");

    assertTrue(phone.waitForText("To bookmark this location",200));
    phone.sendResponse("2");
    
    assertTrue(phone.waitForText("This location has been bookmarked", 10));
    phone.sendDefaultResponse();
    
    assertTrue(phone.waitForText("To return to the main menu",200));
    phone.sendResponse("3");
    
    phone.stop();
    
    /**
     * Check that the bookmark exists in the SMS interface
     */
    SmsTestSupport sms = new SmsTestSupport();
    sms.start();
    
    sms.url("%23bookmarks");
    assertTrue(sms.isTextPresent("1: 15th Ave NW & NW Market St"));
    assertTrue(sms.isTextPresent("Text \"onebus #bookmark num\" to see bookmark."));
    
    sms.stop();
    
    /**
     * Add another bookmark using the phone interface
     */
    phone = new PhoneTestSupport();
    phone.setResetUser(false);
    phone.start();

    assertTrue(phone.waitForText("To enter a stop number, please press one.", 20));
    phone.sendResponse("1");
    
    assertTrue(phone.waitForText("Please enter your stop number",10));
    phone.sendLongResponse("13760#");

    assertTrue(phone.waitForText("To bookmark this location",200));
    phone.sendResponse("2");
    
    assertTrue(phone.waitForText("This location has been bookmarked", 10));
    phone.sendDefaultResponse();
    
    assertTrue(phone.waitForText("To return to the main menu",200));
    phone.sendResponse("3");
    
    phone.stop();
    
    /**
     * Check that the bookmark exists in the SMS interface
     */
    sms = new SmsTestSupport();
    sms.start();
    
    sms.url("%23bookmarks");
    assertTrue(sms.isTextPresent("1: 15th Ave NW & NW Market St"));
    assertTrue(sms.isTextPresent("1: 15th Ave NW & NW Leary Way"));
    assertTrue(sms.isTextPresent("Text \"onebus #bookmark num\" to see bookmark."));
    
    sms.stop();
  }
}
