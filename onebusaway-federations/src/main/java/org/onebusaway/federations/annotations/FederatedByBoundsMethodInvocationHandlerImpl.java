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
 * the {@link FederatedByBoundsMethod} annotation.
 * 
 * @author bdferris
 */
class FederatedByBoundsMethodInvocationHandlerImpl implements FederatedServiceMethodInvocationHandler {

  private int _lat1ArgumentIndex;

  private int _lon1ArgumentIndex;

  private int _lat2ArgumentIndex;

  private int _lon2ArgumentIndex;

  public FederatedByBoundsMethodInvocationHandlerImpl(int lat1ArgumentIndex, int lon1ArgumentIndex,
      int lat2ArgumentIndex, int lon2ArgumentIndex) {
    _lat1ArgumentIndex = lat1ArgumentIndex;
    _lon1ArgumentIndex = lon1ArgumentIndex;
    _lat2ArgumentIndex = lat2ArgumentIndex;
    _lon2ArgumentIndex = lon2ArgumentIndex;
  }

  public int getLat1ArgumentIndex() {
    return _lat1ArgumentIndex;
  }

  public int getLon1ArgumentIndex() {
    return _lon1ArgumentIndex;
  }

  public int getLat2ArgumentIndex() {
    return _lat2ArgumentIndex;
  }

  public int getLon2ArgumentIndex() {
    return _lon2ArgumentIndex;
  }

  public Object invoke(FederatedServiceCollection collection, Method method, Object[] args)
      throws ServiceAreaServiceException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

    double lat1 = arg(args, _lat1ArgumentIndex);
    double lon1 = arg(args, _lon1ArgumentIndex);
    double lat2 = arg(args, _lat2ArgumentIndex);
    double lon2 = arg(args, _lon2ArgumentIndex);

    FederatedService service = collection.getServiceForBounds(lat1, lon1, lat2, lon2);
    return method.invoke(service, args);
  }

  private static final double arg(Object[] args, int index) {
    return ((Double) args[index]).doubleValue();
  }

}
