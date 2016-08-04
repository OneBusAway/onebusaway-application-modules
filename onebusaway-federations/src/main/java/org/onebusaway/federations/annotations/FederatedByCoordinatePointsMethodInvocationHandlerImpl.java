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
package org.onebusaway.federations.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
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

    List<CoordinatePoint> points = new ArrayList<CoordinatePoint>();

    for (int i = 0; i < _argumentIndices.length; i++) {
      Object value = args[_argumentIndices[i]];
      PropertyPathExpression expression = _expressions[i];
      if (expression != null)
        value = expression.invoke(value);
      CoordinatePoint point = (CoordinatePoint) value;
      points.add(point);
    }

    FederatedService service = collection.getServiceForLocations(points);
    return method.invoke(service, args);
  }
}
