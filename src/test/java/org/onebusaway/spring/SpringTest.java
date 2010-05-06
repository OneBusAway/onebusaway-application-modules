package org.onebusaway.spring;

import org.junit.Test;
import org.onebusaway.common.impl.UtilityLibrary;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class SpringTest {
  @Test
  public void testGo() {
    List<String> paths = new ArrayList<String>();
    paths.add("classpath:/org/onebusaway/spring/SpringTest.xml");
    ApplicationContext context = UtilityLibrary.createContext(paths);
  }
}
