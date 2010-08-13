package org.onebusaway.transit_data_federation.bundle.tasks;

import java.util.List;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreatorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Given an instantiated and running instance of a federated transit data
 * bundle, we pre-cache a number of expensive data operations so that the cache
 * will be hot for eventual deployment.
 * 
 * That includes caching {@link StopBean}, {@link RouteBean} and
 * {@link StopsForRouteBean} objects, which in turn cache a number of other
 * things.
 * 
 * @author bdferris
 * 
 */
public class PreCacheTask implements FederatedTransitDataBundleCreatorTask {

  private static Logger _log = LoggerFactory.getLogger(PreCacheTask.class);

  private TransitDataService _service;

  @Autowired
  public void setTransitDataService(TransitDataService service) {
    _service = service;
  }

  @Override
  public void run() {

    try {
      List<AgencyWithCoverageBean> agenciesWithCoverage = _service.getAgenciesWithCoverage();

      for (AgencyWithCoverageBean agencyWithCoverage : agenciesWithCoverage) {

        AgencyBean agency = agencyWithCoverage.getAgency();
        System.out.println("agency=" + agency.getId());

        ListBean<String> stopIds = _service.getStopIdsForAgencyId(agency.getId());
        for (String stopId : stopIds.getList()) {
          System.out.println("  stop=" + stopId);
          _service.getStop(stopId);
        }

        ListBean<String> routeIds = _service.getRouteIdsForAgencyId(agency.getId());
        for (String routeId : routeIds.getList()) {
          System.out.println("  route=" + routeId);
          _service.getStopsForRoute(routeId);
        }
      }
    } catch (ServiceException ex) {
      _log.error("service exception", ex);
    }

  }
}
