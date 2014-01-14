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
package org.onebusaway.federations;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandler;
import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandlerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * A Spring {@link FactoryBean} for creating a proxied service interface that
 * virtually dispatches to a set of {@link FederatedService} instances, as
 * contained by a {@link FederatedServiceCollection}.
 * 
 * <pre class="code">
 * <bean class="org.onebusaway.federations.FederatedServiceFactoryBean">
 *   <property name="serviceInterface" value="some.package.SomeServiceInterface"/>
 *   <proeprty name="collection" ref="federatedServiceCollectionImpl" />
 * </bean>
 * </pre>
 * 
 * @author bdferris
 * @see FederatedService
 * @see FederatedServiceCollection
 */
public class FederatedServiceFactoryBean extends AbstractFactoryBean<Object> {

  private static FederatedServiceMethodInvocationHandlerFactory _handlerFactory = new FederatedServiceMethodInvocationHandlerFactory();

  private Class<?> _serviceInterface;

  private FederatedServiceCollection _collection;

  public void setServiceInterface(Class<?> serviceInterface) {
    _serviceInterface = serviceInterface;
  }

  public void setCollection(FederatedServiceCollection collection) {
    _collection = collection;
  }

  @Override
  public Class<?> getObjectType() {
    return _serviceInterface;
  }

  @Override
  protected Object createInstance() throws Exception {

    if (!_serviceInterface.isInterface())
      throw new IllegalArgumentException("service "
          + _serviceInterface.getName() + " is not an interface");

    if (!FederatedService.class.isAssignableFrom(_serviceInterface))
      throw new IllegalArgumentException("service interface "
          + _serviceInterface.getName() + " does not implement "
          + FederatedService.class);

    Map<Method, FederatedServiceMethodInvocationHandler> methodHandlers = getMethodHandlers();

    FederatedServiceInvocationHandler handler = new FederatedServiceInvocationHandler(
        methodHandlers, _collection);

    Class<?>[] interfaces = {_serviceInterface};
    return Proxy.newProxyInstance(_serviceInterface.getClassLoader(),
        interfaces, handler);
  }

  private Map<Method, FederatedServiceMethodInvocationHandler> getMethodHandlers() {
    Map<Method, FederatedServiceMethodInvocationHandler> handlers = new HashMap<Method, FederatedServiceMethodInvocationHandler>();
    for (Method method : _serviceInterface.getDeclaredMethods()) {
      FederatedServiceMethodInvocationHandler handler = _handlerFactory.getHandlerForMethod(method);
      handlers.put(method, handler);
    }
    return handlers;
  }

}
