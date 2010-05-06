package org.onebusaway.common.impl;

import com.vividsolutions.jts.geom.Point;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

public class UtilityLibrary {

  private static final String CLASSPATH_PREFIX = "classpath:";

  private static final String FILE_PREFIX = "file:";

  public static final double distance(Point a, Point b) {
    return Math.sqrt(p2(a.getX() - b.getX()) + p2(a.getY() - b.getY()));
  }

  private static final double p2(double a) {
    return a * a;
  }

  public static ApplicationContext createContext(Iterable<String> paths) {
    GenericApplicationContext ctx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);

    for (String path : paths) {
      if (path.startsWith(CLASSPATH_PREFIX)) {
        path = path.substring(CLASSPATH_PREFIX.length());
        xmlReader.loadBeanDefinitions(new ClassPathResource(path));
      } else if (path.startsWith(FILE_PREFIX)) {
        path = path.substring(FILE_PREFIX.length());
        xmlReader.loadBeanDefinitions(new FileSystemResource(path));
      } else {
        xmlReader.loadBeanDefinitions(new ClassPathResource(path));
      }
    }
    ctx.refresh();
    ctx.registerShutdownHook();
    return ctx;
  }
}
