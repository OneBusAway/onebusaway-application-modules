package org.onebusaway.federations.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.geospatial.model.CoordinateBounds;

/**
 * Provides a {@link FederatedServiceMethodInvocationHandler} implementation for
 * the {@link FederatedByCoordinateBoundsMethod} annotation.
 * 
 * @author bdferris
 */
class FederatedByCoordinateBoundsMethodInvocationHandlerImpl implements
    FederatedServiceMethodInvocationHandler {

  private int _argumentIndex;
  
  private PropertyPathExpression _expression;

  public int getArgumentIndex() {
    return _argumentIndex;
  }
  
  public PropertyPathExpression getExpression() {
    return _expression;
  }

  public FederatedByCoordinateBoundsMethodInvocationHandlerImpl(Method method,
      int argumentIndex, String expression) {
    _argumentIndex = argumentIndex;
    if (expression != null && expression.length() > 0) {
      _expression = new PropertyPathExpression(expression);
      Class<?>[] parameterTypes = method.getParameterTypes();
      _expression.initialize(parameterTypes[argumentIndex]);
    }
  }

  public Object invoke(FederatedServiceCollection collection, Method method,
      Object[] args) throws ServiceAreaServiceException,
      IllegalArgumentException, IllegalAccessException,
      InvocationTargetException {

    Object value = args[_argumentIndex];

    if (_expression != null)
      value = _expression.invoke(value);

    CoordinateBounds bounds = (CoordinateBounds) value;

    FederatedService service = collection.getServiceForBounds(bounds);
    return method.invoke(service, args);
  }
}
