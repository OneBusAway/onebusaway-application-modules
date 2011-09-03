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

import org.onebusaway.utility.ObjectSerializationLibrary;

import org.springframework.aop.target.AbstractLazyCreationTargetSource;

import java.io.File;
import java.lang.reflect.Method;

/**
 * A Spring {@link FactoryBean} for lazily instantiating a serialized bean from
 * a file as needed
 * 
 * @author bdferris
 */
public class SerializedLazyCreationTargetSource extends
    AbstractLazyCreationTargetSource {

  private File _path;
  private String _initMethod;

  public void setPath(File path) {
    _path = path;
  }

  public void setInitMethod(String method) {
    _initMethod = method;
  }

  @Override
  protected Object createObject() throws Exception {
    Object obj = ObjectSerializationLibrary.readObject(_path);
    if (_initMethod != null) {
      Class<? extends Object> c = obj.getClass();
      for (Method method : c.getMethods()) {
        if (method.getName().equals(_initMethod))
          if (method.getParameterTypes().length > 0) {
            System.err.println("cannot call init method " + _initMethod
                + " on " + obj + " because method requires arguments");
          } else {
            method.invoke(obj);
          }
      }
    }
    return obj;
  }
}
