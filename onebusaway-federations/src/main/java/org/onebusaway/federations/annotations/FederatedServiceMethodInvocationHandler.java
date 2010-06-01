package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.FederatedServiceFactoryBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Defines a {@link FederatedService} method invocation handler. The handler is
 * responsible for examining the arguments of the method invocation to determine
 * an appropriate {@link FederatedService} from the passed
 * {@link FederatedServiceCollection} for handling the method.
 * 
 * The {@link FederatedServiceMethodInvocationHandlerFactory} can be used to
 * examine a Method signature and create an appropriate
 * {@link FederatedServiceMethodInvocationHandler} based on method annotations.
 * 
 * @author bdferris
 * @see FederatedServiceMethodInvocationHandlerFactory
 * @see FederatedServiceFactoryBean
 */
public interface FederatedServiceMethodInvocationHandler {

  public Object invoke(FederatedServiceCollection collection, Method method,
      Object[] args) throws ServiceException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException;
}
