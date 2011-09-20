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
package org.onebusaway.presentation.impl.resources;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ResourcePrototype;

public class ClientBundleImpl implements ClientBundle, InvocationHandler {

  private static Logger _log = LoggerFactory.getLogger(ClientBundleImpl.class);

  private Map<String, ResourcePrototype> _resources = new HashMap<String, ResourcePrototype>();

  private Class<?> _bundleType;

  public ClientBundleImpl(Class<?> bundleType) {
    _bundleType = bundleType;
  }

  public String getName() {
    return _bundleType.getName();
  }

  public void addResource(ResourcePrototype resource) {
    _resources.put(resource.getName(), resource);
  }

  public ResourcePrototype getResource(String name) {
    return _resources.get(name);
  }

  /*****************************************************************************
   * {@link InvocationHandler} Interface
   ****************************************************************************/

  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {

    if (!_resources.containsKey(method.getName())) {
      try {
        return method.invoke(this, args);
      } catch (Throwable ex) {
        _log.warn("unkonwn resource method: " + method + " bundleType="
            + _bundleType.getName() + " resources=" + _resources);;
        throw new IllegalStateException("unknown resource method: "
            + method.getName(), ex);
      }
    }
    return _resources.get(method.getName());
  }
}
