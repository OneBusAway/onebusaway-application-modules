package org.onebusaway.federations;

import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class FederatedServiceInvocationHandler implements InvocationHandler {

  private Map<Method, FederatedServiceMethodInvocationHandler> _methodHandlers = new HashMap<Method, FederatedServiceMethodInvocationHandler>();

  private FederatedServiceRegistry _registry;

  public FederatedServiceInvocationHandler(Map<Method, FederatedServiceMethodInvocationHandler> methodHandlers,
      FederatedServiceRegistry registry) {
    _methodHandlers = methodHandlers;
    _registry = registry;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    FederatedServiceMethodInvocationHandler methodHandler = _methodHandlers.get(method);
    return methodHandler.invoke(_registry, method, args);
  }

}
