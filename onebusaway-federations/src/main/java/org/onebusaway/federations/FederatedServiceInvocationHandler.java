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

import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * An {@link InvocationHandler} for use in a {@link Proxy} service that can
 * create a virtual service interface instance that dispatches method calls to
 * an appropriate {@link FederatedService} instance as queryed from a
 * {@link FederatedServiceCollection}. Each method in the service interface must
 * have a {@link FederatedServiceMethodInvocationHandler} that determines how
 * the method invocation is resolved to a federated service instance.
 * 
 * @author bdferris
 */
class FederatedServiceInvocationHandler implements InvocationHandler {

  private Map<Method, FederatedServiceMethodInvocationHandler> _methodHandlers = new HashMap<Method, FederatedServiceMethodInvocationHandler>();

  private FederatedServiceCollection _collection;

  public FederatedServiceInvocationHandler(
      Map<Method, FederatedServiceMethodInvocationHandler> methodHandlers,
      FederatedServiceCollection collection) {
    _methodHandlers = methodHandlers;
    _collection = collection;
  }

  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
    FederatedServiceMethodInvocationHandler methodHandler = _methodHandlers.get(method);

    if (method.getDeclaringClass() == Object.class)
      return method.invoke(this, args);

    try {
      return methodHandler.invoke(_collection, method, args);
    } catch (Throwable ex) {
      if (ex instanceof InvocationTargetException) {
        InvocationTargetException ite = (InvocationTargetException) ex;
        ex = ite.getTargetException();
      }
      throw ex;
    }
  }

}
