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

import org.onebusaway.collections.beans.PropertyPathExpression;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides a {@link FederatedServiceMethodInvocationHandler} implementation for
 * the {@link FederatedByEntityIdMethod} annotation.
 * 
 * @author bdferris
 */
class FederatedByEntityIdMethodInvocationHandlerImpl implements
    FederatedServiceMethodInvocationHandler {

  private int _argumentIndex;

  private PropertyPathExpression _expression = null;

  public FederatedByEntityIdMethodInvocationHandlerImpl(Method method,
      int argumentIndex, String expression) {
    _argumentIndex = argumentIndex;
    if (expression != null && expression.length() > 0) {
      _expression = new PropertyPathExpression(expression);
      Class<?>[] parameterTypes = method.getParameterTypes();
      _expression.initialize(parameterTypes[argumentIndex]);
    }
  }

  public int getArgumentIndex() {
    return _argumentIndex;
  }

  public Object invoke(FederatedServiceCollection collection, Method method,
      Object[] args) throws ServiceException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
    Object value = args[_argumentIndex];
    if (_expression != null)
      value = _expression.invoke(value);
    String entityId = value.toString();
    String agencyId = AgencyIdSupport.getAgencyIdFromEntityId(entityId);
    FederatedService service = collection.getServiceForAgencyId(agencyId);
    return method.invoke(service, args);
  }
}
