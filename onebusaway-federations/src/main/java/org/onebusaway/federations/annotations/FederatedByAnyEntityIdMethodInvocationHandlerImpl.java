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

import org.onebusaway.collections.PropertyPathExpression;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides a {@link FederatedServiceMethodInvocationHandler} implementation for
 * the {@link FederatedByEntityIdsMethod} annotation.
 * 
 * @author bdferris
 */
class FederatedByAnyEntityIdMethodInvocationHandlerImpl implements
    FederatedServiceMethodInvocationHandler {

  private final int _argumentIndex;

  private final List<PropertyPathExpression> _expressions;

  public FederatedByAnyEntityIdMethodInvocationHandlerImpl(Method method,
      int argumentIndex, String[] properties) {

    if (properties.length == 0)
      throw new IllegalArgumentException(
          "you must specify at least one property expression");

    _argumentIndex = argumentIndex;
    _expressions = new ArrayList<PropertyPathExpression>(properties.length);

    for (String property : properties) {
      PropertyPathExpression expression = new PropertyPathExpression(property);
      Class<?>[] parameterTypes = method.getParameterTypes();
      expression.initialize(parameterTypes[argumentIndex]);
      _expressions.add(expression);
    }
  }

  public int getArgumentIndex() {
    return _argumentIndex;
  }

  public Object invoke(FederatedServiceCollection collection, Method method,
      Object[] args) throws ServiceException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {

    Object target = args[_argumentIndex];

    Set<String> agencyIds = new HashSet<String>();
    for (PropertyPathExpression expression : _expressions) {
      String entityId = (String) expression.invoke(target);
      if( entityId != null)
        agencyIds.add(AgencyIdSupport.getAgencyIdFromEntityId(entityId));
    }

    FederatedService service = collection.getServiceForAgencyIds(agencyIds);
    return method.invoke(service, args);
  }
}
