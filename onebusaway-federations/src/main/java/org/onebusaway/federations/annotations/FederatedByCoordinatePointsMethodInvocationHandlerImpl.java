package org.onebusaway.federations.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;

/**
 * Provides a {@link FederatedServiceMethodInvocationHandler} implementation for
 * the {@link FederatedByCoordinatePointsMethod} annotation.
 * 
 * @author bdferris
 */
class FederatedByCoordinatePointsMethodInvocationHandlerImpl implements
    FederatedServiceMethodInvocationHandler {

  private int[] _argumentIndices;

  private PropertyPathExpression[] _expressions;

  public int[] getArgumentIndices() {
    return _argumentIndices;
  }

  public PropertyPathExpression[] getExpressions() {
    return _expressions;
  }

  public FederatedByCoordinatePointsMethodInvocationHandlerImpl(Method method,
      int[] argumentIndices, String[] expressions) {

    if (argumentIndices.length != expressions.length)
      throw new IllegalArgumentException(
          "the number of argument indices and expressions must be the same");

    _argumentIndices = argumentIndices;
    _expressions = new PropertyPathExpression[expressions.length];

    for (int i = 0; i < expressions.length; i++) {
      String expression = expressions[i];
      if (expression != null && expression.length() > 0) {
        _expressions[i] = new PropertyPathExpression(expression);
        Class<?>[] parameterTypes = method.getParameterTypes();
        _expressions[i].initialize(parameterTypes[argumentIndices[i]]);
      }
    }
  }

  public Object invoke(FederatedServiceCollection collection, Method method,
      Object[] args) throws ServiceAreaServiceException,
      IllegalArgumentException, IllegalAccessException,
      InvocationTargetException {

    CoordinateBounds bounds = new CoordinateBounds();

    for (int i = 0; i < _argumentIndices.length; i++) {
      Object value = args[_argumentIndices[i]];
      PropertyPathExpression expression = _expressions[i];
      if (expression != null)
        value = expression.invoke(value);
      CoordinatePoint point = (CoordinatePoint) value;
      bounds.addPoint(point.getLat(), point.getLon());
    }

    FederatedService service = collection.getServiceForBounds(bounds);
    return method.invoke(service, args);
  }
}
