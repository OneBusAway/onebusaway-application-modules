package org.onebusaway.federations;

import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class FederatedServiceInvocationHandler implements InvocationHandler {

  private Map<Method, FederatedServiceMethodInvocationHandler> _methodHandlers = new HashMap<Method, FederatedServiceMethodInvocationHandler>();

  private FederatedServiceCollection _collection;

  public FederatedServiceInvocationHandler(Map<Method, FederatedServiceMethodInvocationHandler> methodHandlers,
      FederatedServiceCollection collection) {
    _methodHandlers = methodHandlers;
    _collection = collection;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    FederatedServiceMethodInvocationHandler methodHandler = _methodHandlers.get(method);
    
    if( method.getDeclaringClass() == Object.class)
      return method.invoke(this, args);
    
    try {
      return methodHandler.invoke(_collection, method, args);
    }
    catch(Throwable ex) {
      if( ex instanceof InvocationTargetException) {
          InvocationTargetException ite = (InvocationTargetException) ex;
          ex = ite.getTargetException();
      }
      throw ex;
    }
  }

}
