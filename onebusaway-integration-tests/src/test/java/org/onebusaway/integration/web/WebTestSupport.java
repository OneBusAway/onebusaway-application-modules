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

  @Override
  public void open(String message) {
    open(url(message), true);
  }

  protected String url(String url) {
    StringBuilder b = new StringBuilder();
    if (_prefix != null)
      b.append(_prefix);
    b.append(url);
    return b.toString();
  }
}
