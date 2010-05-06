package org.onebusaway.federations.annotations;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

class FederatedByAgencyIdsMethodInvocationHandlerImpl implements FederatedServiceMethodInvocationHandler {

  private int _argumentIndex;

  public FederatedByAgencyIdsMethodInvocationHandlerImpl(int argumentIndex) {
    _argumentIndex = argumentIndex;
  }

  public int getArgumentIndex() {
    return _argumentIndex;
  }

  @SuppressWarnings("unchecked")
  public Object invoke(FederatedServiceRegistry registry, Method method, Object[] args) throws ServiceException,
      IllegalArgumentException, IllegalAccessException, InvocationTargetException {

    Iterable<String> entityIds = (Iterable<String>) args[_argumentIndex];
    Set<String> agencyIds = new HashSet<String>();
    for (String entityId : entityIds)
      agencyIds.add(AgencyIdSupport.getAgencyIdFromEntityId(entityId));
    FederatedService service = registry.getServiceForAgencyIds(agencyIds);
    return method.invoke(service, args);
  }

}
