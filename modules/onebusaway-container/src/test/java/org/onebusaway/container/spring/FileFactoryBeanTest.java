package org.onebusaway.container.spring;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.File;

public class FileFactoryBeanTest {
  
  @Test
  public void test() throws Exception {
    System.setProperty("toReplace", "b");
    System.setProperty("toReplaceAgain", "d");
    FileFactoryBean factory = new FileFactoryBean();
    factory.setPath("/a/${toReplace}/c/${toReplaceAgain}/e");
    File file = (File) factory.getObject();
    assertEquals("/a/b/c/d/e",file.getAbsolutePath());
  }
}
