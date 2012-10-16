/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2012 Google, Inc.
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.onebusaway.exceptions.ServiceException;

/**
 * This factory can be used to examine a Method signature and create an
 * appropriate {@link FederatedServiceMethodInvocationHandler} based on method
 * annotations. See the list of supported annotations below.
 * 
 * @author bdferris
 * @see FederatedByAgencyIdMethod
 * @see FederatedByAggregateMethod
 * @see FederatedByBoundsMethod
 * @see FederatedByCoordinateBoundsMethod
 * @see FederatedByCoordinatePointsMethod
 * @see FederatedByEntityIdMethod
 * @see FederatedByEntityIdsMethod
 * @see FederatedByLocationMethod
 */
public class FederatedServiceMethodInvocationHandlerFactory {

  public FederatedServiceMethodInvocationHandler getHandlerForMethod(
      Method method) {

    FederatedByAgencyIdMethod byAgency = method.getAnnotation(FederatedByAgencyIdMethod.class);
    if (byAgency != null)
      return new FederatedByAgencyIdMethodInvocationHandlerImpl(method,
          byAgency.argument(), byAgency.propertyExpression());

    FederatedByAnyEntityIdMethod byAnyEntityId = method.getAnnotation(FederatedByAnyEntityIdMethod.class);
    if (byAnyEntityId != null)
      return new FederatedByAnyEntityIdMethodInvocationHandlerImpl(method,
          byAnyEntityId.argument(), byAnyEntityId.properties(),
          byAnyEntityId.agencyIdProperties());

    FederatedByEntityIdMethod ann0 = method.getAnnotation(FederatedByEntityIdMethod.class);
    if (ann0 != null)
      return new FederatedByEntityIdMethodInvocationHandlerImpl(method,
          ann0.argument(), ann0.propertyExpression());

    FederatedByEntityIdsMethod ann1 = method.getAnnotation(FederatedByEntityIdsMethod.class);
    if (ann1 != null)
      return new FederatedByEntityIdsMethodInvocationHandlerImpl(
          ann1.argument());

    FederatedByBoundsMethod ann2 = method.getAnnotation(FederatedByBoundsMethod.class);
    if (ann2 != null)
      return new FederatedByBoundsMethodInvocationHandlerImpl(
          ann2.lat1Argument(), ann2.lon1Argument(), ann2.lat2Argument(),
          ann2.lon2Argument());

    FederatedByLocationMethod ann3 = method.getAnnotation(FederatedByLocationMethod.class);
    if (ann3 != null)
      return new FederatedByLocationMethodInvocationHandlerImpl(
          ann3.latArgument(), ann3.lonArgument());

    FederatedByAggregateMethod ann4 = method.getAnnotation(FederatedByAggregateMethod.class);
    if (ann4 != null) {
      EMethodAggregationType aggregationType = getAggregationTypeForMethod(method);
      return new FederatedByAggregateMethodInvocationHandlerImpl(
          aggregationType);
    }

    FederatedByCoordinateBoundsMethod ann5 = method.getAnnotation(FederatedByCoordinateBoundsMethod.class);
    if (ann5 != null)
      return new FederatedByCoordinateBoundsMethodInvocationHandlerImpl(method,
          ann5.argument(), ann5.propertyExpression());

    FederatedByCoordinatePointsMethod ann6 = method.getAnnotation(FederatedByCoordinatePointsMethod.class);
    if (ann6 != null) {
      int[] argumentIndices = ann6.arguments();
      String[] expressions = ann6.propertyExpressions();
      if (expressions.length == 0)
        expressions = new String[argumentIndices.length];
      return new FederatedByCoordinatePointsMethodInvocationHandlerImpl(method,
          argumentIndices, expressions);
    }

    FederatedByCustomMethod ann7 = method.getAnnotation(FederatedByCustomMethod.class);
    if (ann7 != null) {
      Class<? extends FederatedServiceMethodInvocationHandler> handlerClass = ann7.handler();
      try {
        return handlerClass.newInstance();
      } catch (Exception ex) {
        throw new ServiceException(
            "error creating FederatedServiceMethodInvocationHandler of type "
                + handlerClass, ex);
      }
    }

    throw new IllegalArgumentException(
        "No FederatedService method annotation for method: " + method);
  }

  private EMethodAggregationType getAggregationTypeForMethod(Method method) {
    Class<?> returnType = method.getReturnType();
    if (List.class.isAssignableFrom(returnType))
      return EMethodAggregationType.LIST;
    if (Map.class.isAssignableFrom(returnType))
      return EMethodAggregationType.MAP;
    throw new IllegalArgumentException("unsupported aggregation type: "
        + returnType.getName());
  }

}
