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

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * A Spring {@link BeanPostProcessor} to add additional values to
 * already-created {@link Map} object.
 * 
 * @author bdferris
 * @see ListBeanPostProcessor
 * @see PropertiesBeanPostProcessor
 */
public class MapBeanPostProcessor implements BeanPostProcessor, Ordered {

  private int _order;

  private String _target;

  private Map<Object, Object> _values;

  public void setTarget(String target) {
    _target = target;
  }

  public void setValues(Map<Object, Object> values) {
    _values = values;
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

  @SuppressWarnings("unchecked")
  public Object postProcessAfterInitialization(Object obj, String beanName)
      throws BeansException {

    if (_values != null && beanName.equals(_target)) {
      if (obj instanceof Map) {
        Map<Object, Object> objects = (Map<Object, Object>) obj;
        objects.putAll(_values);
      } else if (obj instanceof MapFactoryBean) {
        MapFactoryBean factory = (MapFactoryBean) obj;
        factory.putValues(_values);
      }
    }

    return obj;
  }

}
