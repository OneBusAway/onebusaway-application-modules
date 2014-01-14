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
package org.onebusaway.federations;

import java.util.List;
import java.util.Set;

import org.onebusaway.exceptions.ServiceAreaServiceException;
import org.onebusaway.federations.impl.DynamicFederatedServiceCollectionImpl;
import org.onebusaway.federations.impl.FederatedServiceCollectionImpl;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;

/**
 * A collection of {@link FederatedService} instances. Provides interface for
 * querying instances by agency id and geographic location. A basic
 * implementation can be found in {@link FederatedServiceCollectionImpl}, which
 * takes a map of {@link FederatedService} instances and provides the require
 * querying mechanims.
 * 
 * More powerful implementations exist as well. The
 * {@link DynamicFederatedServiceCollectionImpl} is backed by a
 * {@link FederatedServiceRegistry} to dynamically update the set of available
 * {@link FederatedService} instances, potentially from a remote source.
 * 
 * @author bdferris
 * @see FederatedServiceCollectionImpl
 * @see DynamicFederatedServiceCollectionImpl
 */
public interface FederatedServiceCollection {

  public abstract Set<FederatedService> getAllServices();

  public abstract FederatedService getServiceForAgencyId(String agencyId)
      throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForAgencyIds(
      Iterable<String> agencyIds) throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForBounds(CoordinateBounds bounds)
      throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForBounds(double lat1,
      double lon1, double lat2, double lon2) throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForLocation(double lat, double lon)
      throws ServiceAreaServiceException;

  public abstract FederatedService getServiceForLocations(
      List<CoordinatePoint> points) throws ServiceAreaServiceException;
}