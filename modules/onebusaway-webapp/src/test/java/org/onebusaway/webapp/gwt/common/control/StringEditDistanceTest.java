package org.onebusaway.webapp.gwt.common.control;

import com.google.gwt.junit.client.GWTTestCase;

public class StringEditDistanceTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "org.onebusaway.common.web.common.CommonLibrary";
  }

  public void test01() {
    assertEquals(0, StringEditDistance.getEditDistance("a", "a"));
    assertEquals(1, StringEditDistance.getEditDistance("a", "b"));
    assertEquals(0, StringEditDistance.getEditDistance("aa", "aa"));
    assertEquals(1, StringEditDistance.getEditDistance("aa", "ab"));
    assertEquals(1, StringEditDistance.getEditDistance("aa", "aac"));
    assertEquals(1, StringEditDistance.getEditDistance("ct", "cat"));
  }

}
