/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.common.spring;

import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

public class OverrideBeanPostProcessor implements BeanPostProcessor, Ordered {

  private int _order;

  private String _targetName;

  private Class<?> _targetClass;

  private Map<String, Object> _preModifiers;

  private Map<String, Object> _postModifiers;
  
  public OverrideBeanPostProcessor() {
    System.out.println("===================> " + OverrideBeanPostProcessor.class);
  }

  public void setTargetName(String targetName) {
    _targetName = targetName;
  }

  public void setTargetClass(Class<?> targetClass) {
    _targetClass = targetClass;
  }

  public void setPreModifiers(Map<String, Object> modifiers) {
    _preModifiers = modifiers;
  }

  public void setPostModifiers(Map<String, Object> modifiers) {
    _postModifiers = modifiers;
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

  public Object postProcessBeforeInitialization(Object obj, String beanName)
      throws BeansException {

    System.out.println("bean=" + beanName + " obj=" + obj);

    if (!isApplicable(obj, beanName))
      return obj;

    System.out.println("PRE name=" + beanName + " value=" + obj);

    if (_preModifiers != null && !_preModifiers.isEmpty()) {
      BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(obj);
      wrapper.setPropertyValues(_preModifiers);
    }

    return obj;
  }

  public Object postProcessAfterInitialization(Object obj, String beanName)
      throws BeansException {

    if (!isApplicable(obj, beanName))
      return obj;

    System.out.println("POST name=" + beanName + " value=" + obj);

    if (_postModifiers != null && !_postModifiers.isEmpty()) {
      BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(obj);
      wrapper.setPropertyValues(_postModifiers);
    }

    return obj;
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private boolean isApplicable(Object obj, String beanName) {

    if (_targetName != null && _targetName.equals(beanName))
      return true;

    if (obj == null)
      return false;

    if (_targetClass != null && _targetClass.isAssignableFrom(obj.getClass()))
      return true;

    return false;
  }
}
