package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripBeanServiceImpl implements TripBeanService {

  private TransitGraphDao _graph;

  private NarrativeService _narrativeService;

  private RouteBeanService _routeBeanService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setRouteBeanService(RouteBeanService routeBeanService) {
    _routeBeanService = routeBeanService;
  }

  @Cacheable
  public TripBean getTripForId(AgencyAndId tripId) {

    TripEntry tripEntry = _graph.getTripEntryForId(tripId);

    if (tripEntry == null)
      return null;

    AgencyAndId routeId = tripEntry.getRouteCollectionId();
    RouteBean routeBean = _routeBeanService.getRouteForId(routeId);

    TripNarrative tripNarrative = _narrativeService.getTripForId(tripId);

    TripBean tripBean = new TripBean();

    tripBean.setId(ApplicationBeanLibrary.getId(tripId));

    tripBean.setTripShortName(tripNarrative.getTripShortName());
    tripBean.setTripHeadsign(tripNarrative.getTripHeadsign());
    tripBean.setRoute(routeBean);
    tripBean.setRouteShortName(tripNarrative.getRouteShortName());
    tripBean.setServiceId(ApplicationBeanLibrary.getId(tripEntry.getServiceId()));

    AgencyAndId shapeId = tripNarrative.getShapeId();
    if (shapeId != null && shapeId.hasValues())
      tripBean.setShapeId(ApplicationBeanLibrary.getId(shapeId));

    tripBean.setDirectionId(tripNarrative.getDirectionId());

    tripBean.setBlockId(tripNarrative.getBlockId());
    
    return tripBean;
  }
}
