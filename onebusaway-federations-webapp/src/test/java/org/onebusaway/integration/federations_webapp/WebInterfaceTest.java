package org.onebusaway.integration.federations_webapp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.DefaultSelenium;

public class WebInterfaceTest {

  private DefaultSelenium selenium;

  @Before
  public void setUp() throws Exception {
    int port = Integer.parseInt(System.getProperty(
        "org.onebusaway.federations_webapp.port", "8080"));
    System.out.println("port=" + port);
    selenium = new DefaultSelenium("localhost", port, "*firefox", "/onebusaway-federations-webapp");
  }

  @Test
  public void testUntitled() throws Exception {
    selenium.open("/onebusaway-federations-webapp/remoting/index.action");
    selenium.type("url", "http://onebusaway.org/");
    selenium.type("serviceClass", "org.onebusaway.TransitData");
    selenium.click("submit");
    selenium.waitForPageToLoad("30000");
    assertEquals("org.onebusaway.TransitData",
        selenium.getText("//tr[2]/td[1]"));
    assertEquals("http://onebusaway.org/", selenium.getText("//tr[2]/td[2]/a"));
    assertEquals("true", selenium.getText("//tr[2]/td[3]"));
    selenium.click("//tr[2]/td[5]/a");
    selenium.waitForPageToLoad("30000");
    assertEquals("false", selenium.getText("//tr[2]/td[3]"));
    selenium.click("//tr[2]/td[6]/a");
    selenium.waitForPageToLoad("30000");
    assertFalse(selenium.isTextPresent("org.onebusaway.TransitData"));
  }
}
