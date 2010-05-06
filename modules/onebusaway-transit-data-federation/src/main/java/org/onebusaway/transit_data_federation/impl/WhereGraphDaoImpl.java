package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.exceptions.NoSuchEntityServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.WhereGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class WhereGraphDaoImpl implements WhereGraphDao {

  private TripPlannerGraph _graph;

  @Autowired
  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public Set<AgencyAndId> getRouteCollectionIdsForStop(AgencyAndId stopId)
      throws NoSuchEntityServiceException {

    StopEntry stopEntry = _graph.getStopEntryForId(stopId);
    if (stopEntry == null)
      throw new NoSuchEntityServiceException();

    StopTimeIndex index = stopEntry.getStopTimes();

    Set<AgencyAndId> routeCollectionIds = new HashSet<AgencyAndId>();

    for (AgencyAndId serviceId : index.getServiceIds()) {
      List<StopTimeEntry> stopTimes = index.getStopTimesForServiceIdSortedByArrival(serviceId);
      for (StopTimeEntry stopTime : stopTimes) {
        TripEntry trip = stopTime.getTrip();
        routeCollectionIds.add(trip.getRouteCollectionId());
      }
    }

    return routeCollectionIds;
  }
}
