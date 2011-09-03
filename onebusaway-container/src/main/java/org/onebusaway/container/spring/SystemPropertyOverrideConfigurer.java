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
package org.onebusaway.container.spring;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.PropertiesLoaderSupport;

/**
 * A Spring {@link BeanFactoryPostProcessor} that can be used to override System
 * properties (see {@link System#getProperties()}) from within a Spring
 * application context. Because it is a {@link BeanFactoryPostProcessor} and
 * also implements {@link PriorityOrdered}, this processor can be configured to
 * run before almost anything else when constructing a Spring application
 * context, especially if you set the {@code order} property to something low.
 * This is useful for setting System properties before another
 * {@link BeanFactoryPostProcessor} like {@link PropertyPlaceholderConfigurer}
 * or {@link PropertyOverrideConfigurer} are applied.
 * 
 * @author bdferris
 * @see BeanFactoryPostProcessor
 * @see PropertiesLoaderSupport
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 */
public class SystemPropertyOverrideConfigurer extends PropertiesLoaderSupport
    implements BeanFactoryPostProcessor, PriorityOrdered {

  private int _order = Ordered.LOWEST_PRECEDENCE;

  public void setOrder(int order) {
    _order = order;
  }

  @Override
  public int getOrder() {
    return _order;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {

    try {
      Properties properties = mergeProperties();
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        String key = entry.getKey().toString();
        String value = entry.getValue().toString();
        System.setProperty(key, value);
      }
    } catch (IOException ex) {
      throw new FatalBeanException("bad", ex);
    }
  }

}
