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
package org.onebusaway.webapp.impl;

import org.springframework.beans.factory.FactoryBean;

public abstract class GWTAbstractFactory implements FactoryBean {

  protected Class<?> _type;

  private boolean _singleton = false;

  private Object _instance = null;

  private Class<?> _parentType;

  public GWTAbstractFactory(Class<?> parentType) {
    _parentType = parentType;
  }

  public void setType(Class<?> type) {
    if (!_parentType.isAssignableFrom(type))
      throw new IllegalStateException("type is not assignable to "
          + _parentType);
    _type = type;
  }

  public void setSingleton(boolean singleton) {
    _singleton = singleton;
  }

  /*****************************************************************************
   * {@link FactoryBean} Interface
   ****************************************************************************/

  public Class<?> getObjectType() {
    return _type;
  }

  public boolean isSingleton() {
    return _singleton;
  }

  public synchronized Object getObject() throws Exception {
    if (!_singleton)
      return createInstance();
    if (_instance == null)
      _instance = createInstance();
    return _instance;
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  protected abstract Object createInstance() throws Exception;
}
