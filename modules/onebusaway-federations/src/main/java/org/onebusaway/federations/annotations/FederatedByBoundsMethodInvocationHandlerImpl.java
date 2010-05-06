package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

  public Object invoke(FederatedServiceRegistry registry, Method method, Object[] args)
      throws ServiceAreaServiceException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

    double lat1 = arg(args, _lat1ArgumentIndex);
    double lon1 = arg(args, _lon1ArgumentIndex);
    double lat2 = arg(args, _lat2ArgumentIndex);
    double lon2 = arg(args, _lon2ArgumentIndex);

    FederatedService service = registry.getServiceForBounds(lat1, lon1, lat2, lon2);
    return method.invoke(service, args);
  }

  private static final double arg(Object[] args, int index) {
    return ((Double) args[index]).doubleValue();
  }

}
