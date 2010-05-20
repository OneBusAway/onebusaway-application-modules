package org.onebusaway.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

public class ContainerLibrary {

  private static final String CLASSPATH_PREFIX = "classpath:";

  private static final String FILE_PREFIX = "file:";

  public static ConfigurableApplicationContext createContext(String... paths) {
    List<String> list = new ArrayList<String>();
    for (String path : paths)
      list.add(path);
    return createContext(list);
  }

  public static ConfigurableApplicationContext createContext(
      Iterable<String> paths) {
    return createContext(paths, new HashMap<String, BeanDefinition>());
  }

  public static ConfigurableApplicationContext createContext(
      Iterable<String> paths, Map<String, BeanDefinition> additionalBeans) {

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

    for (Map.Entry<String, BeanDefinition> entry : additionalBeans.entrySet())
      ctx.registerBeanDefinition(entry.getKey(), entry.getValue());

    ctx.refresh();
    ctx.registerShutdownHook();
    return ctx;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getBeanOfType(ApplicationContext context,
      Class<T> beanType) {
    Map<String, T> beans = context.getBeansOfType(beanType);
    if (beans.size() == 0)
      throw new IllegalStateException("no beans of type " + beanType.getName());
    if (beans.size() > 1)
      throw new IllegalStateException("multiple beans of type "
          + beanType.getName());
    return beans.values().iterator().next();
  }
}
