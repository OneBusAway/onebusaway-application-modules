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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * A Spring {@link FactoryBean} for creating a {@link List} object.
 * 
 * @author bdferris
 * @see MapFactoryBean
 */
public class MapFactoryBean extends AbstractFactoryBean<Map<Object, Object>> {

  private Map<Object, Object> _values = new HashMap<Object, Object>();

  @Override
  public Class<?> getObjectType() {
    return Map.class;
  }

  public void setValues(Map<Object, Object> values) {
    _values = values;
  }

  @Override
  protected Map<Object, Object> createInstance() throws Exception {
    return new HashMap<Object, Object>(_values);
  }

  public void putValues(Map<Object, Object> values) {
    _values.putAll(values);
  }
}
