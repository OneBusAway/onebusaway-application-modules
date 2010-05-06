package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

class FederatedByEntityIdsMethodInvocationHandlerImpl implements
    FederatedServiceMethodInvocationHandler {

  private int _argumentIndex;

  public FederatedByEntityIdsMethodInvocationHandlerImpl(int argumentIndex) {
    _argumentIndex = argumentIndex;
  }

  public int getArgumentIndex() {
    return _argumentIndex;
  }

  @SuppressWarnings("unchecked")
  public Object invoke(FederatedServiceCollection collection, Method method,
      Object[] args) throws ServiceException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {

    Iterable<String> entityIds = (Iterable<String>) args[_argumentIndex];
    Set<String> agencyIds = new HashSet<String>();
    for (String entityId : entityIds)
      agencyIds.add(AgencyIdSupport.getAgencyIdFromEntityId(entityId));
    FederatedService service = collection.getServiceForAgencyIds(agencyIds);
    return method.invoke(service, args);
  }

}
