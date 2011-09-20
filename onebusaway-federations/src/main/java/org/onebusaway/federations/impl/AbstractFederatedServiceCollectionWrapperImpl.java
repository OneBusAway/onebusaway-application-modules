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
package org.onebusaway.federations.impl;

import java.util.List;
import java.util.Set;

import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.FederatedService;
import org.onebusaway.federations.FederatedServiceCollection;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;

/**
 * {@link FederatedServiceCollection} wrapper class that allows one to pass
 * calls from one collection instance to another.
 * 
 * @author bdferris
 */
public abstract class AbstractFederatedServiceCollectionWrapperImpl implements
    FederatedServiceCollection {

  @Override
  public Set<FederatedService> getAllServices() {
    return getCollection().getAllServices();
  }

  @Override
  public FederatedService getServiceForAgencyId(String agencyId)
      throws ServiceAreaServiceException {
    return getCollection().getServiceForAgencyId(agencyId);
  }

  @Override
  public FederatedService getServiceForAgencyIds(Iterable<String> agencyIds)
      throws ServiceAreaServiceException {
    return getCollection().getServiceForAgencyIds(agencyIds);
  }

  @Override
  public FederatedService getServiceForBounds(CoordinateBounds bounds)
      throws ServiceAreaServiceException {
    return getCollection().getServiceForBounds(bounds);
  }

  @Override
  public FederatedService getServiceForBounds(double lat1, double lon1,
      double lat2, double lon2) throws ServiceAreaServiceException {
    return getCollection().getServiceForBounds(lat1, lon1, lat2, lon2);
  }

  @Override
  public FederatedService getServiceForLocation(double lat, double lon)
      throws ServiceAreaServiceException {
    return getCollection().getServiceForLocation(lat, lon);
  }

  @Override
  public FederatedService getServiceForLocations(List<CoordinatePoint> points)
      throws ServiceAreaServiceException {
    return getCollection().getServiceForLocations(points);
  }

  /****
   * Protected Methods
   ****/

  protected abstract FederatedServiceCollection getCollection();
}
