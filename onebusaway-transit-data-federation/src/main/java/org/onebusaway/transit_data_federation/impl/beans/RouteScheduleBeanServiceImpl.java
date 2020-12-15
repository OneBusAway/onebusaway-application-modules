/**
 * Copyright (C) 2020 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data.model.RouteScheduleBean;
import org.onebusaway.transit_data.model.StopTimeInstanceBean;
import org.onebusaway.transit_data.model.StopTripDirectionBean;
import org.onebusaway.transit_data_federation.impl.DirectedGraph;
import org.onebusaway.transit_data_federation.impl.StopGraphComparator;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.beans.RouteScheduleBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.AgencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

/**
 * Support Schedule queries at the route level.  Similar to
 * StopScheduleBeanServiceImpl.
 *
 *   Ultimate goal to deliver data in this format:
 *     "entry": {
 *   "routeId": "40_100479",
 *   "serviceIds": ["SERVICEIDVALUE1","SERVICEIDVALUE2"],
 *   "scheduleDate": 1609315200,
 *   "stopTripGroupings": [
 *     {
 *       "directionId": 0,
 *       "tripHeadsign": "University of Washington Station",
 *       "stopIds": ["STOPID1", "STOPID2"],
 *       "tripIds": ["TRIPID1", "TRIPID2"]
 *     },
 *     {
 *       "directionId": 1,
 *       "tripHeadsign": "Angle Lake Station",
 *       "stopIds": ["STOPID2", "STOPID3"],
 *       "tripIds": ["TRIPID3", "TRIPID4"]
 *     }
 *   ]
 * }
 */
public class RouteScheduleBeanServiceImpl implements RouteScheduleBeanService {

  protected TransitGraphDao _graph;
  protected ExtendedCalendarService _calendarService;
  protected BlockIndexService _blockIndexService;
  private NarrativeService _narrativeService;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao graph) {
    _graph = graph;
  }

  @Autowired
  public void setNarrativeService(NarrativeService service) {
    _narrativeService = service;
  }

  @Autowired
  public void setCalendarService(ExtendedCalendarService service) {
    _calendarService = service;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Override
  @Cacheable
  public RouteScheduleBean getScheduledArrivalsForDate(AgencyAndId routeId, ServiceDate scheduleDate) {
    RouteScheduleBean rsb = new RouteScheduleBean();
    rsb.setRouteId(routeId);
    rsb.setScheduleDate(scheduleDate);
    RouteCollectionEntry routeCollectionForId = _graph.getRouteCollectionForId(routeId);
    if (routeCollectionForId == null) return rsb;

    addStopTripDirectionsViaBlockTrip(rsb, routeId);

    return rsb;
  }



  /*
   * starting with the route collection, populate beans representing service
   * for the given service scheduleDate
  */
  private void addStopTripDirectionsViaBlockTrip(RouteScheduleBean rsb, AgencyAndId routeId) {
    List<BlockTripIndex> blockTripIndices = _blockIndexService.getBlockTripIndicesForRouteCollectionId(routeId);

    Map<DirectionalHeadsign, StopTripDirectionBean> headsignToBeanMap = new HashMap<>();
    Map<DirectionalHeadsign, StopCollections> headsignToStopCollectionMap = new HashMap<>();
    Set<AgencyAndId> serviceIds = new HashSet<>();
    Set<TripEntry> trips = new LinkedHashSet<>();

    for (BlockTripIndex bti : blockTripIndices) {

      for (BlockTripEntry blockTrip : bti.getTrips()) {

        ServiceIdActivation idActivation = blockTrip.getPattern().getServiceIds();

        if (_calendarService.areServiceIdsActiveOnServiceDate(
                idActivation,
                rsb.getScheduleDate().getAsDate(idActivation.getTimeZone()))) {

          trips.add(blockTrip.getTrip());

          String directionId = blockTrip.getTrip().getDirectionId();
          TripNarrative tripNarrativeForId = _narrativeService.getTripForId(blockTrip.getTrip().getId());
          String headsign = null;
          if (tripNarrativeForId != null) {
            headsign = tripNarrativeForId.getTripHeadsign();
          } else {
            headsign = getDestinationForTrip(blockTrip.getTrip());
          }
          DirectionalHeadsign dh = new DirectionalHeadsign(directionId, headsign);
          if (!headsignToBeanMap.containsKey(dh)) {
            StopTripDirectionBean stdb = new StopTripDirectionBean();
            stdb.setDirectionId(directionId);
            stdb.setTripHeadsign(headsign);
            stdb.setTripIds(new ArrayList<>());
            stdb.getTripIds().add(blockTrip.getTrip().getId());
            stdb.setStopIds(new ArrayList<>());
            headsignToBeanMap.put(dh, stdb);

            // build up a list of stopping patterns
            StopCollection stops = new StopCollection();
            stops.addFromTrip(blockTrip.getTrip());
            StopCollections stopCollections = new StopCollections();
            stopCollections.addIfNotPresent(stops);
            headsignToStopCollectionMap.put(dh, stopCollections);

          } else {
            StopTripDirectionBean stdb = headsignToBeanMap.get(dh);
            stdb.getTripIds().add(blockTrip.getTrip().getId());

            StopCollection stops = new StopCollection();
            stops.addFromTrip(blockTrip.getTrip());
            StopCollections stopCollections = headsignToStopCollectionMap.get(dh);
            stopCollections.addIfNotPresent(stops);
          }
          serviceIds.add(blockTrip.getTrip().getServiceId().getId());
        }
      }
    }

    // collapse StopCollections down to canonical pattern
    for (DirectionalHeadsign dh : headsignToBeanMap.keySet()) {
      StopTripDirectionBean bean = headsignToBeanMap.get(dh);
      bean.setStopIds(collapse(headsignToStopCollectionMap.get(dh)));
      bean.getStopTimes().addAll(getStopTimesForTrips(trips, bean.getTripIds(), rsb.getScheduleDate()));
    }

    rsb.setServiceIds(new ArrayList<>());
    for (AgencyAndId serviceId : serviceIds) {
      rsb.getServiceIds().add(serviceId);
    }

    rsb.getStopTripDirections().addAll(headsignToBeanMap.values());

  }

  private List<StopTimeInstanceBean> getStopTimesForTrips(Set<TripEntry> allTrips, List<AgencyAndId> selectedTrips, ServiceDate serviceDate) {
    ArrayList<StopTimeInstanceBean> stopTimes = new ArrayList<>();
    for (AgencyAndId selection : selectedTrips) {
      for (TripEntry entry : allTrips) {
        if (entry.getId().equals(selection))
          stopTimes.addAll(beanify(entry.getStopTimes(), serviceDate));
      }
    }
    return stopTimes;
  }

  private List<StopTimeInstanceBean> beanify(List<StopTimeEntry> stopTimes, ServiceDate serviceDate) {
    if (stopTimes == null) return Collections.emptyList();
    ArrayList<StopTimeInstanceBean> beans = new ArrayList<>();

    for (StopTimeEntry entry : stopTimes) {
      StopTimeInstanceBean bean = new StopTimeInstanceBean();
      bean.setTripId(AgencyAndIdLibrary.convertToString(entry.getTrip().getId()));
      bean.setServiceId(AgencyAndIdLibrary.convertToString(entry.getTrip().getServiceId().getId()));
      bean.setServiceDate(serviceDate.getAsDate(getTimeZoneForAgency(entry.getTrip().getId().getAgencyId())).getTime());
      bean.setArrivalEnabled(entry.getArrivalTime() > 0);
      bean.setArrivalTime(entry.getArrivalTime());
      bean.setDepartureEnabled(entry.getDepartureTime() > 0);
      bean.setDepartureTime(entry.getDepartureTime());
      beans.add(bean);
    }
    return beans;
  }

  private TimeZone getTimeZoneForAgency(String agencyId) {
    AgencyNarrative agency = _narrativeService.getAgencyForId(agencyId);
    if (agency == null) return TimeZone.getDefault();
    if (agency.getTimezone() == null) return TimeZone.getDefault();
    return TimeZone.getTimeZone(agency.getTimezone());
  }

  // this algorithm comes from RouteBeanServiceImpl.getStopsInOrder
  // it fails on very complex shapes, but otherwise makes a best
  // effort guess at a canonical stopping pattern for a route
  private List<AgencyAndId> collapse(StopCollections stopCollections) {
    List<StopEntry> stopsInDefaultOrder = new ArrayList<>();
    DirectedGraph<StopEntry> graph = new DirectedGraph<>();
    for (StopCollection sequence : stopCollections.getList()) {
      StopEntry prev = null;
      for (StopEntry stop : sequence.getStops()) {
        if (prev != null) {
          // We do this to avoid cycles
          if (!graph.isConnected(stop, prev)) {
            graph.addEdge(prev, stop);
            // this tells us if the stop is already used
            stopsInDefaultOrder.add(stop);
          }
        }
        prev = stop;
      }
    }
    // here we guess at a canonical route pattern via a topological sort order
    // works well for simple routes, does poorly for loops
    StopGraphComparator c = new StopGraphComparator(graph);
    List<AgencyAndId> ids = new ArrayList<>();
    for ( StopEntry entry : graph.getTopologicalSort(c)) {
      ids.add(entry.getId());
    }
    return ids;
  }

  private String getDestinationForTrip(TripEntry trip) {
    int lastStopIndex = trip.getStopTimes().size() -1;
    AgencyAndId stopId = trip.getStopTimes().get(lastStopIndex).getStop().getId();
    StopNarrative stopForId = _narrativeService.getStopForId(stopId);
    if (stopForId == null)
      return null;
    return stopForId.getName();
  }


  private static class DirectionalHeadsign {
    private String directionId;
    private String tripHeadsign;

    public DirectionalHeadsign(
            String directionId,
            String tripHeadsign) {
      this.directionId = directionId;
      this.tripHeadsign = tripHeadsign;
    }
    public String getDirectionId() {
      return directionId;
    }
    public String getTripHeadsign() {
      return tripHeadsign;
    }
  }

  private static class StopCollections {
    private List<StopCollection> list = new ArrayList<>();
    public StopCollections() {
    }

    public void addIfNotPresent(StopCollection stops) {
      if (!list.contains(stops))
        list.add(stops);
    }


    public List<StopCollection> getList() {
      return list;
    }
  }

  private static class StopCollection {
    private List<StopEntry> stops;
    public StopCollection() {
      stops = new ArrayList<>();
    }
    public StopCollection(List<StopEntry> stops) {
      this.stops = stops;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      StopCollection that = (StopCollection) o;
      return Objects.equals(stops, that.stops);
    }

    @Override
    public int hashCode() {
      return Objects.hash(stops);
    }

    public void addFromTrip(TripEntry trip) {
      for (StopTimeEntry stopTimes : trip.getStopTimes()) {
        stops.add(stopTimes.getStop());
      }

    }

    public List<StopEntry> getStops() {
      return stops;
    }
  }
}
