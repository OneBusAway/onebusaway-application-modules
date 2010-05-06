package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedServiceRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface FederatedServiceMethodInvocationHandler {
  public Object invoke(FederatedServiceRegistry registry, Method method, Object[] args)
      throws ServiceException, IllegalArgumentException, IllegalAccessException, InvocationTargetException;
}
