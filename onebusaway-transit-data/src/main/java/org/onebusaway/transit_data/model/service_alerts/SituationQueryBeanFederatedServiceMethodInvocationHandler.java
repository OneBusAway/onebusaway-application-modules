/**
 * Copyright (C) 2012 Google, Inc.
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
package org.onebusaway.transit_data.model.service_alerts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.federations.annotations.AgencyIdSupport;
import org.onebusaway.federations.annotations.FederatedServiceMethodInvocationHandler;
import org.onebusaway.transit_data.services.TransitDataService;

/**
 * A custom {@link FederatedServiceMethodInvocationHandler} for handling
 * federated dispatch of method that accept a single {@link SituationQueryBean}
 * as an argument, such as
 * {@link TransitDataService#getServiceAlerts(SituationQueryBean)}.
 * 
 * @author bdferris
 * @see TransitDataService#getServiceAlerts(SituationQueryBean)
 * @see SituationQueryBean
 * @see FederatedServiceMethodInvocationHandler
 */
public class SituationQueryBeanFederatedServiceMethodInvocationHandler
    implements FederatedServiceMethodInvocationHandler {

  @Override
  public Object invoke(FederatedServiceCollection collection, Method method,
      Object[] args) throws ServiceException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
    if (args.length == 0) {
      throw new ServiceException("unexpected number of arguments");
    }
    SituationQueryBean query = (SituationQueryBean) args[0];
    Set<String> agencyIds = new HashSet<String>();
    if (query.getAffects() != null) {
      for (SituationQueryBean.AffectsBean affects : query.getAffects()) {
        if (affects.getAgencyId() != null) {
          agencyIds.add(affects.getAgencyId());
        }
        addAgencyId(affects.getRouteId(), agencyIds);
        addAgencyId(affects.getTripId(), agencyIds);
        addAgencyId(affects.getStopId(), agencyIds);
      }
    }
    FederatedService service = collection.getServiceForAgencyIds(agencyIds);
    return method.invoke(service, args);
  }

  private static void addAgencyId(String entityId, Set<String> agencyIds) {
    if (entityId == null || entityId.isEmpty()) {
      return;
    }
    agencyIds.add(AgencyIdSupport.getAgencyIdFromEntityId(entityId));
  }

}
