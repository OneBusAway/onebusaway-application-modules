package org.onebusaway.federations;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandler;
import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandlerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class FederatedServiceFactoryBean extends AbstractFactoryBean {

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
