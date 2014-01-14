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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.Ordered;

/**
 * A Spring {@link BeanPostProcessor} to add additional values to
 * already-created {@link Properties} object.
 * 
 * @author bdferris
 * @see ListBeanPostProcessor
 * @see PropertiesBeanPostProcessor
 */
public class PropertiesBeanPostProcessor implements BeanPostProcessor, Ordered {

  private int _order;

  private Set<String> _targets = new HashSet<String>();

  private Properties _properties;

  public void setTarget(String target) {
    setTargets(Arrays.asList(target));
  }

  public void setTargets(List<String> targets) {
    _targets.clear();
    _targets.addAll(targets);
  }

  public void setProperties(Properties properties) {
    _properties = properties;
  }

  /***************************************************************************
   * {@link Ordered} Interface
   **************************************************************************/

  public void setOrder(int order) {
    _order = order;
  }

  public int getOrder() {
    return _order;
  }

  /***************************************************************************
   * {@link BeanPostProcessor} Interface
   **************************************************************************/

  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }

  public Object postProcessAfterInitialization(Object obj, String beanName)
      throws BeansException {

    if (_properties != null && _targets.contains(beanName)) {
      if (obj instanceof Properties) {
        Properties properties = (Properties) obj;
        properties.putAll(_properties);
      } else if (obj instanceof PropertiesFactoryBean) {
        PropertiesFactoryBean factory = (PropertiesFactoryBean) obj;
        System.out.println(factory);
      }
    }

    return obj;
  }

}