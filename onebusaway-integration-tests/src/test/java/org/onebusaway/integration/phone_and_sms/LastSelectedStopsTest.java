package org.onebusaway.integration.phone_and_sms;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.onebusaway.integration.TestSupport;
import org.onebusaway.integration.phone.PhoneTestSupport;
import org.onebusaway.integration.sms.SmsTestSupport;

public class LastSelectedStopsTest {

  @Test
  public void test() throws IOException {
    
    PhoneTestSupport phone = new PhoneTestSupport();
    phone.start();

    assertTrue(phone.waitForText("To check your most recent stop, please press five.", 20));
    phone.sendResponse("5");
    
    assertTrue(phone.waitForText("You have no previous stop to check.",10));
    phone.sendDefaultResponse();
    
    phone.stop();
    
    /**
     * Request a stop on the SMS interface
     */
    SmsTestSupport sms = new SmsTestSupport();
    sms.start();
    
    sms.url("%23reset");
    assertTrue(sms.isTextPresent("Reset successful."));
    
    sms.url("13721");
    assertTrue(sms.isTextPresent("Please enter your zip code so we can provide results appropriate to your location."));

    sms.url("98105");
    int hourOfDay = TestSupport.getHourOfDay();
    boolean checkArrivals = 5 <= hourOfDay && hourOfDay < 24;
    if (checkArrivals)
      assertTrue(sms.isTextPresent("15 DwntwnSeattle:"));
    
    sms.stop();
    
    /**
     * Add another bookmark using the phone interface
     */
    phone = new PhoneTestSupport();
    phone.setResetUser(false);
    phone.start();
    
    assertTrue(phone.waitForText("To check your most recent stop, please press five.", 20));
    phone.sendResponse("5");
    
    if( checkArrivals ) {
      assertTrue(phone.waitForText("route", 10));
      phone.sendDefaultResponse();
    }
    
    assertTrue(phone.waitForText("To return to the main menu",200,true));
    phone.sendResponse("3");
    
    phone.stop();
  }
}
