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
package org.onebusaway.common.web.gwt.resources.impl;

import org.onebusaway.common.web.gwt.resources.ImmutableResourceBundle;
import org.onebusaway.common.web.gwt.resources.ResourcePrototype;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ImmutableResourceBundleImpl implements ImmutableResourceBundle,
    InvocationHandler {

  private Map<String, ResourcePrototype> _resources = new HashMap<String, ResourcePrototype>();

  private String _bundleName;

  public ImmutableResourceBundleImpl(String bundleName) {
    _bundleName = bundleName;
  }

  public String getName() {
    return _bundleName;
  }

  public void addResource(ResourcePrototype resource) {
    _resources.put(resource.getName(), resource);
  }

  /*****************************************************************************
   * {@link ImmutableResourceBundle} Interface
   ****************************************************************************/

  public ResourcePrototype getResource(String name) {
    return _resources.get(name);
  }

  public ResourcePrototype[] getResources() {
    return _resources.values().toArray(new ResourcePrototype[_resources.size()]);
  }

  /*****************************************************************************
   * {@link InvocationHandler} Interface
   ****************************************************************************/

  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {

    if (!_resources.containsKey(method.getName())) {
      try {
        Method parentMethod = getClass().getMethod(method.getName(),
            method.getParameterTypes());
        return parentMethod.invoke(this, args);
      } catch (NoSuchMethodException ex) {
        throw new IllegalStateException("unknown resource method: "
            + method.getName());
      }
    }
    return _resources.get(method.getName());
  }
}
