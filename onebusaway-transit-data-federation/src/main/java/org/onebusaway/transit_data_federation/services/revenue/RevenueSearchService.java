package org.onebusaway.transit_data_federation.services.revenue;

public interface RevenueSearchService {
  public Boolean stopHasRevenueServiceOnRoute(String agencyId, String stopId,
      String routeId, String directionId);
  
  public Boolean stopHasRevenueService(String agencyId, String stopId);
}
