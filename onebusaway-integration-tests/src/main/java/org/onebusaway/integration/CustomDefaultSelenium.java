/**
 * 
 */
package org.onebusaway.integration;

import com.thoughtworks.selenium.DefaultSelenium;

public class CustomDefaultSelenium extends DefaultSelenium {

  public CustomDefaultSelenium(String browserUrl, String browserStartCommand) {
    super("localhost", 4444, browserStartCommand, browserUrl);
  }

  @Override
  public void open(String url) {
    open(url, true);
  }

  public void open(String url, boolean noHeadRequest) {
    commandProcessor.doCommand("open", new String[] {
        url, Boolean.toString(noHeadRequest)});
  }
}