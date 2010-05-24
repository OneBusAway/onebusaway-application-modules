package org.onebusaway.integration.federations_webapp;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleneseTestCase;

public class WebInterfaceTest extends SeleneseTestCase {

  @Before
  public void setUp() throws Exception {
    String port = System.getProperty("org.onebusaway.federations_webapp.port", "8080");
    System.out.println("port=" + port);
    setUp("http://localhost:" + port + "/", "*firefox");
  }

  @Test
  public void testUntitled() throws Exception {
    selenium.open("/onebusaway-federations-webapp/remoting/index.action");
    selenium.type("url", "http://onebusaway.org/");
    selenium.type("serviceClass", "org.onebusaway.TransitData");
    selenium.click("submit");
    selenium.waitForPageToLoad("30000");
    verifyEquals("org.onebusaway.TransitData",
        selenium.getText("//tr[2]/td[1]"));
    verifyEquals("http://onebusaway.org/", selenium.getText("//tr[2]/td[2]/a"));
    verifyEquals("true", selenium.getText("//tr[2]/td[3]"));
    selenium.click("//tr[2]/td[5]/a");
    selenium.waitForPageToLoad("30000");
    verifyEquals("false", selenium.getText("//tr[2]/td[3]"));
    selenium.click("//tr[2]/td[6]/a");
    selenium.waitForPageToLoad("30000");
    verifyFalse(selenium.isTextPresent("org.onebusaway.TransitData"));
  }
}
