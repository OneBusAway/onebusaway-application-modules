package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class FederatedByEntityIdMethodInvocationHandlerImpl implements FederatedServiceMethodInvocationHandler {

  private int _argumentIndex;

  public FederatedByEntityIdMethodInvocationHandlerImpl(int argumentIndex) {
    _argumentIndex = argumentIndex;
  }

  public int getArgumentIndex() {
    return _argumentIndex;
  }

  public Object invoke(FederatedServiceCollection collection, Method method, Object[] args) throws ServiceException,
      IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    String entityId = (String) args[_argumentIndex];
    String agencyId = AgencyIdSupport.getAgencyIdFromEntityId(entityId);
    FederatedService service = collection.getServiceForAgencyId(agencyId);
    return method.invoke(service, args);
  }
}
