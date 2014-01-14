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

import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides a {@link FederatedServiceMethodInvocationHandler} implementation for
 * the {@link FederatedByLocationMethod} annotation.
 * 
 * @author bdferris
 */
class FederatedByLocationMethodInvocationHandlerImpl implements FederatedServiceMethodInvocationHandler {

  private int _latArgumentIndex;

  private int _lonArgumentIndex;

  public FederatedByLocationMethodInvocationHandlerImpl(int latArgumentIndex, int lonArgumentIndex) {
    _latArgumentIndex = latArgumentIndex;
    _lonArgumentIndex = lonArgumentIndex;
  }

  public int getLatArgumentIndex() {
    return _latArgumentIndex;
  }

  public int getLonArgumentIndex() {
    return _lonArgumentIndex;
  }

  public Object invoke(FederatedServiceCollection collection, Method method, Object[] args)
      throws ServiceAreaServiceException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

    double lat = arg(args, _latArgumentIndex);
    double lon = arg(args, _lonArgumentIndex);

    FederatedService service = collection.getServiceForLocation(lat, lon);
    return method.invoke(service, args);
  }

  private static final double arg(Object[] args, int index) {
    return ((Double) args[index]).doubleValue();
  }

}
