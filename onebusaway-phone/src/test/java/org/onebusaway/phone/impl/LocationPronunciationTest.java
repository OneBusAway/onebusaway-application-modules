package org.onebusaway.phone.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LocationPronunciationTest {

  @Test
  public void test() {
    LocationPronunciation pro = new LocationPronunciation();
    String result = pro.modify("seattle, wa 98105");
    assertEquals("seattle, wa 9 8 1 0 5", result);
  }
}
