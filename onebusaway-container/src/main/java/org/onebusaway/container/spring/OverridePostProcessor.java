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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * A link {@link BeanPostProcessor} that allows one to set and override bean
 * properties in a flexible ways before the bean is initialized. This is similar
 * to the functionality of {@link PropertyOverrideConfigurer}, except that it is
 * a Spring bean post processor as opposed to a factory bean, which determines
 * execution order in the application context construction lifecycle and allows
 * one to use bean references for overriding bean properties as opposed to just
 * string values.
 * 
 * A simple example:
 * 
 * <pre class="code">&lt;bean class="org.onebusaway.container.spring.OverridePostProcessor"&gt;
 *   &lt;property name="map"&gt;
 *     &lt;map&gt;
 *       &lt;entry key="beanName.beanProperty" value ="someOtherBeanName" /&gt;
 *     &lt;/map&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 * 
 * @author bdferris
 */
public class OverridePostProcessor implements BeanPostProcessor,
    PriorityOrdered, BeanFactoryAware {

  private Map<String, List<PropertyEntry>> _propertyEntriesByBeanName = new HashMap<String, List<PropertyEntry>>();

  private int _order = Ordered.LOWEST_PRECEDENCE - 2;

  private BeanFactory _beanFactory;

  public void setMap(Map<String, String> map) {
    processMap(map);
  }

  public void setOrder(int order) {
    _order = order;
  }

  @Override
  public int getOrder() {
    return _order;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    _beanFactory = beanFactory;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    List<PropertyEntry> entries = _propertyEntriesByBeanName.get(beanName);
    if (entries != null) {
      for (PropertyEntry entry : entries) {
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        Object value = _beanFactory.getBean(entry.value);
        if( value == null)
          throw new IllegalStateException("could not find bean with name: " + entry.value);
        wrapper.setPropertyValue(entry.propertyName, value);
      }
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName)
      throws BeansException {
    return bean;
  }

  private void processMap(Map<String, String> map) {
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      int index = key.indexOf('.');
      if (index == -1)
        throw new IllegalArgumentException(
            "expected map key of form \"beanName.propertyName\" for " + key);
      String beanName = key.substring(0, index);
      String propertyName = key.substring(index + 1);
      List<PropertyEntry> entries = _propertyEntriesByBeanName.get(beanName);
      if (entries == null) {
        entries = new ArrayList<PropertyEntry>();
        _propertyEntriesByBeanName.put(beanName, entries);
      }
      entries.add(new PropertyEntry(propertyName, entry.getValue()));
    }
  }

  private static class PropertyEntry {
    public PropertyEntry(String propertyName, String value) {
      this.propertyName = propertyName;
      this.value = value;
    }

    private String propertyName;
    private String value;
  }
}
