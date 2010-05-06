package org.onebusaway.container;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.util.Map;

public class ContainerLibrary {

  private static final String CLASSPATH_PREFIX = "classpath:";

  private static final String FILE_PREFIX = "file:";

  public static ApplicationContext createContext(String... paths) {
    return createContext(CollectionsLibrary.getArrayAsIterable(paths));
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

  @SuppressWarnings("unchecked")
  public static <T> T getBeanOfType(ApplicationContext context, Class<T> beanType) {
    Map<String, T> beans = context.getBeansOfType(beanType);
    if (beans.size() == 0)
      throw new IllegalStateException("no beans of type " + beanType.getName());
    if (beans.size() > 1)
      throw new IllegalStateException("multiple beans of type "
          + beanType.getName());
    return beans.values().iterator().next();
  }
}
