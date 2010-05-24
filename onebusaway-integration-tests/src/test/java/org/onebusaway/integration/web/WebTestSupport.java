package org.onebusaway.integration.web;

import org.junit.After;
import org.junit.Before;
import org.onebusaway.integration.CustomDefaultSelenium;

public class WebTestSupport extends CustomDefaultSelenium {

  private String _prefix = "/onebusaway-webapp";

  public WebTestSupport() {
    super("http://localhost:"
        + System.getProperty("org.onebusaway.webapp.port", "9925") + "/",
        "*firefox");
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
    b.append(message);
    open(b.toString(), true);
  }
}
