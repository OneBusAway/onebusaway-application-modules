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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * A Spring {@link FactoryBean} for creating a {@link List} object.
 * 
 * @author bdferris
 * @see MapFactoryBean
 */
public class ListFactoryBean extends AbstractFactoryBean<List<Object>> {

  private List<Object> _values = new ArrayList<Object>();

  public ListFactoryBean() {
    setSingleton(false);
  }

  public void setValues(List<Object> values) {
    _values = values;
  }

  public void addValues(List<Object> values) {
    _values.addAll(values);
  }

  @Override
  protected List<Object> createInstance() throws Exception {
    List<Object> values = new ArrayList<Object>();
    if (_values != null)
      values.addAll(_values);
    return values;
  }

  @Override
  public Class<?> getObjectType() {
    return List.class;
  }
}
