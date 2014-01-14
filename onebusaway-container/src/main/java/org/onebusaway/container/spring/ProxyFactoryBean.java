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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * A Spring {@link FactoryBean} for instantiating a {@link Proxy} instance with
 * a specified target interface type and a {@link InvocationHandler}.
 * 
 * @author bdferris
 * 
 */
public class ProxyFactoryBean extends AbstractFactoryBean<Object> {

  private Class<?> _proxyInterface;

  private InvocationHandler _invocationHandler;

  public void setProxyInterface(Class<?> proxyInterface) {
    _proxyInterface = proxyInterface;
  }

  public void setInvocationHandler(InvocationHandler invocationHandler) {
    _invocationHandler = invocationHandler;
  }

  @Override
  public Class<?> getObjectType() {
    return _proxyInterface;
  }

  @Override
  protected Object createInstance() throws Exception {
    return Proxy.newProxyInstance(getClass().getClassLoader(),
        new Class<?>[] {_proxyInterface}, _invocationHandler);
  }

}
