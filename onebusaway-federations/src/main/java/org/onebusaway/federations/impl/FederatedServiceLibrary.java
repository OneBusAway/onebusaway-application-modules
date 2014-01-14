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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.federations.FederatedService;
import org.onebusaway.geospatial.model.CoordinateBounds;

/**
 * Provides a number of convenience methods for working with
 * {@link FederatedService} instances.
 * 
 * @author bdferris
 * @see FederatedService
 */
public class FederatedServiceLibrary {

  /**
   * Given a list of {@link FederatedService} instances {@code
   * federatedServiceInstances} that implement a target {@code
   * federatedServiceInterface}, query each instance in turn to determine its
   * set of agencies and geographic bounds, verifying that no two distinct
   * federated service instances have overlapping agency ids or geographic
   * bounds. We construct a map from each service instance to its set of agency
   * ids along with their geographic regions.
   * 
   * @param federatedServiceInstances
   * @param federatedServiceInterface
   * @return a map of service instances along with their agency ids and coverage
   *         areas
   */
  public static Map<FederatedService, Map<String, List<CoordinateBounds>>> getFederatedServiceAgencyCoverage(
      List<? extends FederatedService> federatedServiceInstances,
      Class<? extends FederatedService> federatedServiceInterface) {

    Map<FederatedService, Map<String, List<CoordinateBounds>>> byProvider = new HashMap<FederatedService, Map<String, List<CoordinateBounds>>>();

    for (FederatedService service : federatedServiceInstances) {

      if (!federatedServiceInterface.isAssignableFrom(service.getClass()))
        throw new IllegalArgumentException("service provider " + service
            + " not instance of " + federatedServiceInterface.getName());

      Map<String, List<CoordinateBounds>> agencyIdsWithCoverageArea = service.getAgencyIdsWithCoverageArea();

      for (Map.Entry<String, List<CoordinateBounds>> entry : agencyIdsWithCoverageArea.entrySet()) {
        String agencyId = entry.getKey();
        List<CoordinateBounds> coverage = entry.getValue();
        checkAgencyAndCoverageAgainstExisting(byProvider, agencyId, coverage,
            federatedServiceInterface, true);
      }

      byProvider.put(service, agencyIdsWithCoverageArea);
    }

    return byProvider;
  }

  /**
   * Given an existing set of {@link FederatedService} instances along with
   * their set of agency ids and geographic bounds, examine an additional agency
   * id and coverage area from another service instance and verify that there is
   * no overlap.
   * 
   * @param byProvider existing {@link FederatedService} instances along with
   *          their agency ids and coverage areas
   * @param agencyId a new agency id to check
   * @param coverage the coverage area for that agency
   * @param serviceInterface the target service interface
   * @param failHard throw an exception if overlap is found
   * @return true if there is no overlap (good), other false (bad)
   */
  public static boolean checkAgencyAndCoverageAgainstExisting(
      Map<FederatedService, Map<String, List<CoordinateBounds>>> byProvider,
      String agencyId, List<CoordinateBounds> coverage,
      Class<?> serviceInterface, boolean failHard) {

    for (Map<String, List<CoordinateBounds>> other : byProvider.values()) {

      // Check to see if the specified agencyId has already been defined
      // elsewhere
      if (other.containsKey(agencyId)) {
        if (failHard)
          throw new IllegalArgumentException("agency \"" + agencyId
              + "\" is handled by multiple providers for service "
              + serviceInterface.getName());
        else
          return false;
      }

      // Check to see if the agencyId has coverage overlap with an agency from
      // another provider
      for (Map.Entry<String, List<CoordinateBounds>> otherEntry : other.entrySet()) {

        String otherAgencyId = otherEntry.getKey();
        List<CoordinateBounds> otherCoverage = otherEntry.getValue();

        for (CoordinateBounds otherRectangle : otherCoverage) {
          for (CoordinateBounds rectangle : coverage) {

            if (rectangle.intersects(otherRectangle)) {
              if (failHard)
                throw new IllegalArgumentException("agency \"" + agencyId
                    + "\" has overlap with agency \"" + otherAgencyId
                    + "\" in separate service providers of type "
                    + serviceInterface.getName());
              else
                return false;
            }
          }
        }
      }
    }

    return true;
  }
}
