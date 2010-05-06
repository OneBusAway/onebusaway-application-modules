package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

  public Object invoke(FederatedServiceRegistry registry, Method method, Object[] args)
      throws ServiceAreaServiceException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

    double lat = arg(args, _latArgumentIndex);
    double lon = arg(args, _lonArgumentIndex);

    FederatedService service = registry.getServiceForLocation(lat, lon);
    return method.invoke(service, args);
  }

  private static final double arg(Object[] args, int index) {
    return ((Double) args[index]).doubleValue();
  }

}
