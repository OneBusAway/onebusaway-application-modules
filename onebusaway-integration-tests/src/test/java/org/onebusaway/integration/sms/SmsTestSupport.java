package org.onebusaway.integration.sms;

import org.junit.After;
import org.junit.Before;
import org.onebusaway.integration.CustomDefaultSelenium;

public class SmsTestSupport extends CustomDefaultSelenium {

  private String _userId = "1";

  private String _phoneNumber = "%2B12065551234";

  private String _prefix = "/onebusaway-webapp";

  public SmsTestSupport() {
    super("http://localhost:8080/", "*firefox");
  }

  public void setUserId(String userId) {
    _userId = userId;
  }

  public void setPhoneNumber(String phoneNumber) {
    _phoneNumber = phoneNumber;
  }

  public void setPrefix(String prefix) {
    _prefix = prefix;
  }

  @Before
  public void setup() {
    start();
  }
  
  @After
  public void tearDown() {
    stop();
  }
  
  public void url(String message) {
    StringBuilder b = new StringBuilder();
    if (_prefix != null)
      b.append(_prefix);
    b.append("/sms/textmarks.action?userId=").append(_userId).append(
        "&phoneNumber=").append(_phoneNumber).append("&message=").append(
        message);
    open(b.toString(), true);
  }
}
