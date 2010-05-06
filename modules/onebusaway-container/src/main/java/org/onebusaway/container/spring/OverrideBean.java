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
package org.onebusaway.container.spring;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

public class OverrideBean implements InitializingBean {

  private Object _target;

  private Map<String, Object> _overrides;

  public void setTarget(Object target) {
    _target = target;
  }

  public void setOverrides(Map<String, Object> overrides) {
    _overrides = overrides;
  }

  /***************************************************************************
   * {@link InitializingBean} Interface
   **************************************************************************/

  public void afterPropertiesSet() throws Exception {
    System.out.println("override: bean=" + _target + " properties=" + _overrides);
    if (_target != null && _overrides != null) {
      BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(_target);
      wrapper.setPropertyValues(_overrides);
    }
  }

}
