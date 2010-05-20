package org.onebusaway.integration.web;

import org.junit.Test;

public class StopTest extends WebTestSupport {

  @Test
  public void test() {
    url("/where/standard/stop.action?id=1_13721");
    
    
  }
}
