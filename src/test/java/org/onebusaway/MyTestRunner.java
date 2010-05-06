package org.onebusaway;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;

public class MyTestRunner {

  @SuppressWarnings("unchecked")
  public static <T> T create(Class<T> testClass) {

    ContextConfiguration config = testClass.getAnnotation(ContextConfiguration.class);
    if (config == null)
      throw new IllegalStateException();

    ContextLoader loader = createContextLoader(config);
    ApplicationContext context = createContext(testClass, config, loader);

    return (T) context.getAutowireCapableBeanFactory().createBean(testClass);
  }

  private static ContextLoader createContextLoader(ContextConfiguration config) {
    Class<? extends ContextLoader> loaderType = config.loader();
    try {
      return loaderType.newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException("error instantiating loader: "
          + loaderType.getName(), ex);
    }
  }

  private static ApplicationContext createContext(Class<?> clazz,
      ContextConfiguration config, ContextLoader loader) {
    try {
      String[] locations = loader.processLocations(clazz, config.locations());
      return loader.loadContext(locations);
    } catch (Exception ex) {
      throw new IllegalStateException("error loading context", ex);
    }
  }

}
