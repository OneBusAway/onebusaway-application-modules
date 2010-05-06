package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class FederatedByAgencyIdMethodInvocationHandlerImpl implements FederatedServiceMethodInvocationHandler {

  private int _argumentIndex;

  public FederatedByAgencyIdMethodInvocationHandlerImpl(int argumentIndex) {
    _argumentIndex = argumentIndex;
  }

  public int getArgumentIndex() {
    return _argumentIndex;
  }

  public Object invoke(FederatedServiceCollection collection, Method method, Object[] args) throws ServiceException,
      IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    String agencyId = (String) args[_argumentIndex];
    FederatedService service = collection.getServiceForAgencyId(agencyId);
    return method.invoke(service, args);
  }
}
