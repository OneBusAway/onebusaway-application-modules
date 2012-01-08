/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Convenient methods for instantiating a {@link ConfigurableApplicationContext}
 * Spring application container. For more details on the Spring application
 * container, check out:
 * 
 * http://www.springsource.org/
 * 
 * @author bdferris
 * 
 */
public class ContainerLibrary {

  private static final String CLASSPATH_PREFIX = "classpath:";

  private static final String FILE_PREFIX = "file:";

  /**
   * See {@link #createContext(Iterable, Map)}
   * 
   * @param paths resource paths
   * @return a Spring application container context created from the specified
   *         resources
   */
  public static ConfigurableApplicationContext createContext(String... paths) {
    List<String> list = new ArrayList<String>();
    for (String path : paths)
      list.add(path);
    return createContext(list);
  }

  /**
   * See {@link #createContext(Iterable, Map)}
   * 
   * @param paths resource paths
   * @return a Spring application container context created from the specified
   *         resources
   */
  public static ConfigurableApplicationContext createContext(
      Iterable<String> paths) {
    return createContext(paths, new HashMap<String, BeanDefinition>());
  }

  /**
   * Construct and instantiate a Spring application container from the specified
   * resources. By default, paths are treated as references to classpath
   * resources, but you can prefix with {@value #FILE_PREFIX} to specify a
   * file-path resource or {@value #CLASSPATH_PREFIX} to specifically specify a
   * classpath resource.
   * 
   * 
   * A shutdown hook is automatically registered for the application context.
   * 
   * @param paths resource paths
   * @param additionalBeans additional bean defintions to include in the context
   * @return a Spring application container context
   */
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
