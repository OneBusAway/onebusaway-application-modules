package org.onebusaway.federations.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.federations.FederatedService;
import org.onebusaway.geospatial.model.CoordinateBounds;

public class FederatedServiceLibrary {

  public static Map<FederatedService, Map<String, List<CoordinateBounds>>> getFederatedServiceAgencyCoverage(
      List<?> serviceProviders, Class<?> serviceInterface) {

    Map<FederatedService, Map<String, List<CoordinateBounds>>> byProvider = new HashMap<FederatedService, Map<String, List<CoordinateBounds>>>();

    for (Object serviceProvider : serviceProviders) {

      if (!serviceInterface.isAssignableFrom(serviceProvider.getClass()))
        throw new IllegalArgumentException("service provider "
            + serviceProvider + " not instance of "
            + serviceInterface.getName());
      FederatedService service = (FederatedService) serviceProvider;
      Map<String, List<CoordinateBounds>> agencyIdsWithCoverageArea = service.getAgencyIdsWithCoverageArea();

      for (Map.Entry<String, List<CoordinateBounds>> entry : agencyIdsWithCoverageArea.entrySet()) {
        String agencyId = entry.getKey();
        List<CoordinateBounds> coverage = entry.getValue();
        checkAgencyAndCoverageAgainstExisting(byProvider, agencyId, coverage,
            serviceInterface, true);
      }

      byProvider.put(service, agencyIdsWithCoverageArea);
    }

    return byProvider;
  }

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
