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
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data_federation.impl.DirectedGraph;
import org.onebusaway.transit_data_federation.impl.StopGraphComparator;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.ExtendedCalendarService;
import org.onebusaway.transit_data_federation.services.beans.RouteScheduleBeanService;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripIndex;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
@Component
public class RouteScheduleBeanServiceImpl implements RouteScheduleBeanService {

  protected TransitGraphDao _graph;
  protected ExtendedCalendarService _calendarService;
  protected BlockIndexService _blockIndexService;
  protected NarrativeService _narrativeService;
  private ServiceAlertsBeanService _serviceAlertsBeanService;


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

  @Autowired
  public void setServiceAlertsBeanService(ServiceAlertsBeanService service) { _serviceAlertsBeanService = service; }

  @Override
  @Cacheable
  public RouteScheduleBean getScheduledArrivalsForDate(AgencyAndId routeId, ServiceDate scheduleDate) {
    RouteScheduleBean rsb = new RouteScheduleBean();
    rsb.setOutOfServiceBounds(true);
    rsb.setRouteId(routeId);
    rsb.setScheduleDate(scheduleDate);
    RouteCollectionEntry routeCollectionForId = _graph.getRouteCollectionForId(routeId);
    if (routeCollectionForId == null) return rsb;

    addStopTripDirectionsViaBlockTrip(rsb, routeId);

    addSituations(rsb, routeId);

    return rsb;
  }

  private void addSituations(RouteScheduleBean rsb, AgencyAndId routeId) {
    SituationQueryBean sqb = new SituationQueryBean();

    createAffectsBeanInSituationQuerry(sqb).setRouteId(AgencyAndId.convertToString(routeId));

    for(StopBean stopBean : rsb.getStops()){
      SituationQueryBean.AffectsBean routeStopAffects = createAffectsBeanInSituationQuerry(sqb);
      routeStopAffects.setRouteId(AgencyAndId.convertToString(routeId));
      routeStopAffects.setStopId(stopBean.getId());

      createAffectsBeanInSituationQuerry(sqb).setStopId(stopBean.getId());
    }
    List<ServiceAlertBean> serviceAlerts = _serviceAlertsBeanService.getServiceAlerts(sqb);
    rsb.setServiceAlerts(serviceAlerts);
  }

  private SituationQueryBean.AffectsBean createAffectsBeanInSituationQuerry(SituationQueryBean sqb){
    SituationQueryBean.AffectsBean routeAffects = new SituationQueryBean.AffectsBean();
    sqb.getAffects().add(routeAffects);
    return routeAffects;
  }


  /*
   * starting with the route collection, populate beans representing service
   * for the given service scheduleDate
  */
  private void addStopTripDirectionsViaBlockTrip(RouteScheduleBean rsb, AgencyAndId routeId) {
    List<BlockTripIndex> blockTripIndices = _blockIndexService.getBlockTripIndicesForRouteCollectionId(routeId);

    Map<String,StopCollections> directionToStopCollectionsMap = new HashMap<>();
    Map<String, StopsAndTripsForDirectionBean> directionToStopTripDirectionBeanMap = new HashMap<>();
    Set<AgencyAndId> serviceIds = new HashSet<>();
    Set<TripEntry> activeTrips = new LinkedHashSet<>();
    BeanReferences references = new BeanReferences();
    addAgencyReference(references, routeId.getAgencyId());
    addRouteReference(references, routeId);

    for (BlockTripIndex bti : blockTripIndices) {

      for (BlockTripEntry blockTrip : bti.getTrips()) {

        //check that the calendar is active and act on that
        ServiceIdActivation idActivation = blockTrip.getPattern().getServiceIds();
        if (_calendarService.areServiceIdsActiveOnServiceDate(
                idActivation,
                rsb.getScheduleDate().getAsDate(idActivation.getTimeZone()))) {
          rsb.setOutOfServiceBounds(false);
          activeTrips.add(blockTrip.getTrip());

          // get identifying charectoristics of the trip
          String directionId = blockTrip.getTrip().getDirectionId();
          TripNarrative tripNarrativeForId = _narrativeService.getTripForId(blockTrip.getTrip().getId());
          String headsign = null;
          if (tripNarrativeForId != null) {
            headsign = tripNarrativeForId.getTripHeadsign();
          } else {
            headsign = getDestinationForTrip(blockTrip.getTrip());
          }
          //make a relevant StopCollection
          StopCollection stops = new StopCollection();
          stops.addFromTrip(blockTrip.getTrip());
          // remove stops as relevant to hashing of stopTripDirectionBean
          // make a map of stopTripDirectionBeans to StopCollections
          // Condense the stop collections and reset the result as the new stops in stopTripDirectionBean
          StopsAndTripsForDirectionBean stdb = directionToStopTripDirectionBeanMap.get(directionId);
          StopCollections sc = directionToStopCollectionsMap.get(directionId);
          if(stdb == null){
            stdb = new StopsAndTripsForDirectionBean();
            stdb.setDirectionId(directionId);
            stdb.setTripIds(new ArrayList<>());
            stdb.setStopIds(new ArrayList<>());
            directionToStopTripDirectionBeanMap.put(directionId,stdb);

            sc = new StopCollections();
            directionToStopCollectionsMap.put(directionId,sc);
          }
          stdb.getTripIds().add(blockTrip.getTrip().getId());
          stdb.addTripHeadsign(headsign);
          sc.addIfNotPresent(stops);
          serviceIds.add(blockTrip.getTrip().getServiceId().getId());
        }
        if (willServiceIdsBeActiveAfterServiceDate(
                idActivation,
                rsb.getScheduleDate().getAsDate(idActivation.getTimeZone()))){
          rsb.setOutOfServiceBounds(false);
        }
      }
    }

    // collapse StopCollections down to canonical pattern
    for (String direction : directionToStopTripDirectionBeanMap.keySet()) {
      StopsAndTripsForDirectionBean bean = directionToStopTripDirectionBeanMap.get(direction);
      StopCollections sc = directionToStopCollectionsMap.get(direction);
      bean.setStopIds(collapse(directionToStopCollectionsMap.get(direction)));
      addStopTimeReferences(references, bean, activeTrips, bean.getTripIds(), rsb.getScheduleDate());
    }

    rsb.setServiceIds(new ArrayList<>());
    for (AgencyAndId serviceId : serviceIds) {
      rsb.getServiceIds().add(serviceId);
    }

    rsb.getStopTripDirections().addAll(directionToStopTripDirectionBeanMap.values());
    rsb.getAgencies().addAll(references.getAgencies());
    rsb.getRoutes().addAll(references.getRoutes());
    rsb.getTrips().addAll(references.getTrips());
    rsb.getStops().addAll(references.getStops());
    rsb.getStopTimes().addAll(references.getStopTimes());
  }

  private boolean willServiceIdsBeActiveAfterServiceDate(ServiceIdActivation serviceIds,
                                                         Date serviceDate){
    boolean answer = false;
    Set<Date> dates = _calendarService.getDatesForServiceIds(serviceIds);
    for(Date date : dates){
      if(date.after(serviceDate)){
        return true;
      }
    }
    return answer;
  }

  private void addRouteReference(BeanReferences references, AgencyAndId routeId) {
    findOrBuildRouteBean(references,routeId);
  }

  private RouteBean findOrBuildRouteBean(BeanReferences references, AgencyAndId routeId) {
    RouteBean routeBean = references.getRoute(routeId);
    if (routeBean!=null) return routeBean;

    RouteBean.Builder builder = RouteBean.builder();
    builder.setId(AgencyAndIdLibrary.convertToString(routeId));
    AgencyBean agency = references.getAgencyById(routeId.getAgencyId());
    builder.setAgency(agency);
    RouteCollectionNarrative narrative = _narrativeService.getRouteCollectionForId(routeId);
    if (narrative == null) {
      routeBean = builder.create();
      references.getRoutes().add(routeBean);
      return routeBean;
    }
    builder.setColor(narrative.getColor());
    builder.setDescription(narrative.getDescription());
    builder.setLongName(narrative.getLongName());
    builder.setShortName(narrative.getShortName());
    builder.setTextColor(narrative.getTextColor());
    builder.setType(narrative.getType());
    builder.setUrl(narrative.getUrl());
    routeBean = builder.create();
    references.getRoutes().add(routeBean);
    return routeBean;
  }

  private void addStopTimeReferences(BeanReferences references,
                                     StopsAndTripsForDirectionBean stopsAndTripsForDirectionBean,
                                     Set<TripEntry> allTrips,
                                     List<AgencyAndId> selectedTrips,
                                     ServiceDate serviceDate) {

    for (AgencyAndId selection : selectedTrips) {
      for (TripEntry tripEntry : allTrips) {
        if (tripEntry.getId().equals(selection)) {
          addAgencyReference(references, tripEntry.getId().getAgencyId());
          addTripReference(references, tripEntry);
          for (StopTimeEntry stopTimeEntry : tripEntry.getStopTimes()) {
            addStopTimeReference(references, stopsAndTripsForDirectionBean, stopTimeEntry, serviceDate);
            addStopReference(references,stopTimeEntry.getStop(), stopTimeEntry.getTrip().getRoute());
          }
        }
      }
    }
  }

  private void addAgencyReference(BeanReferences references, String agencyId) {
    if (references.hasAgency(agencyId)) return;
    AgencyBean bean = new AgencyBean();
    references.getAgencies().add(bean);
    bean.setId(agencyId);
    AgencyNarrative narrative = _narrativeService.getAgencyForId(agencyId);
    if (narrative == null) {
      return;
    }

    bean.setName(narrative.getName());
    bean.setLang(narrative.getLang());
    bean.setEmail(narrative.getEmail());
    bean.setPhone(narrative.getPhone());
    bean.setDisclaimer(narrative.getDisclaimer());
    bean.setTimezone(narrative.getTimezone());
    bean.setUrl(narrative.getUrl());
    bean.setFareUrl(narrative.getFareUrl());

    return;
  }

  private void addTripReference(BeanReferences references, TripEntry tripEntry) {
    if (references.hasTrip(tripEntry.getId())) return;
    TripBean bean = new TripBean();
    bean.setId(AgencyAndIdLibrary.convertToString(tripEntry.getId()));
    bean.setDirectionId(tripEntry.getDirectionId());
    bean.setServiceId(AgencyAndIdLibrary.convertToString(tripEntry.getServiceId().getId()));
    bean.setBlockId(AgencyAndIdLibrary.convertToString(tripEntry.getBlock().getId()));
    bean.setShapeId(AgencyAndIdLibrary.convertToString(tripEntry.getShapeId()));
    references.getTrips().add(bean);
    TripNarrative narrative = _narrativeService.getTripForId(tripEntry.getId());
    if (narrative == null) return;
    bean.setTripHeadsign(narrative.getTripHeadsign());
    bean.setRouteShortName(narrative.getRouteShortName());
    bean.setTripShortName(narrative.getTripShortName());
    bean.setRoute(findOrBuildRouteBean(references,tripEntry.getRoute().getId()));
  }

  private void addStopReference(BeanReferences references, StopEntry stop,
                                RouteEntry route) {
    StopBean bean = new StopBean();
    bean.setId(AgencyAndIdLibrary.convertToString(stop.getId()));
    if (references.hasStop(stop.getId())){
      StopBean stopBean = references.getStop(stop.getId());
      RouteBean routeBean = findOrBuildRouteBean(references, route.getId());
      if(!stopBean.getRoutes().contains(routeBean)){
        stopBean.getRoutes().add(routeBean);
      }
      return;
    }
    references.getStops().add(bean);
    bean.setId(AgencyAndIdLibrary.convertToString(stop.getId()));
    bean.setLat(stop.getStopLat());
    bean.setLon(stop.getStopLon());
    ArrayList<RouteBean> routes = new ArrayList<>();
    routes.add(findOrBuildRouteBean(references, route.getId()));
    bean.setRoutes(routes);
    StopNarrative narrative = _narrativeService.getStopForId(stop.getId());
    if (narrative == null) return;
    bean.setName(narrative.getName());
    bean.setCode(narrative.getCode());
    bean.setDirection(narrative.getDirection());
    return;
  }

  private void addStopTimeReference(BeanReferences references, StopsAndTripsForDirectionBean stopsAndTripsForDirectionBean,
                                    StopTimeEntry stopTimeEntry, ServiceDate serviceDate) {
    StopTimeInstanceBeanExtendedWithStopId bean = new StopTimeInstanceBeanExtendedWithStopId();
    bean.setTripId(AgencyAndIdLibrary.convertToString(stopTimeEntry.getTrip().getId()));
    bean.setServiceId(AgencyAndIdLibrary.convertToString(stopTimeEntry.getTrip().getServiceId().getId()));
    bean.setServiceDate(serviceDate.getAsDate(getTimeZoneForAgency(stopTimeEntry.getTrip().getId().getAgencyId())).getTime());
    bean.setArrivalEnabled(stopTimeEntry.getArrivalTime() > 0);
    bean.setArrivalTime(stopTimeEntry.getArrivalTime());
    bean.setDepartureEnabled(stopTimeEntry.getDepartureTime() > 0);
    bean.setDepartureTime(stopTimeEntry.getDepartureTime());
    bean.setStopId(stopTimeEntry.getStop().getId());
    references.getStopTimes().add(bean);
    stopsAndTripsForDirectionBean.getStopTimes().add(bean);
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

  private class DirectionHeadsignStops {
    private String directionId;
    private String tripHeadsign;
    private StopCollections stopCollections = new StopCollections();

    public DirectionHeadsignStops(
            String directionId,
            String tripHeadsign,
            StopCollection stopCollection) {
      this.directionId = directionId;
      this.tripHeadsign = tripHeadsign;
      if (stopCollection == null){ throw new NullPointerException("StopCollection cannot be null");}
      this.stopCollections.addIfNotPresent(stopCollection);
    }
    public String getDirectionId() {
      return directionId;
    }
    public String getTripHeadsign() {
      return tripHeadsign;
    }
    public StopCollections getStopCollections() { return stopCollections; }

    public void addStopCollection(StopCollection stopCollection) {
      stopCollections.addIfNotPresent(stopCollection);
    }
    @Override
    public String toString() {
      return directionId + tripHeadsign + stopCollections.getList().get(0).stops.stream()
              .map(x->x.getId().toString()+x.getStopLat()+x.getStopLon()).sorted().reduce((x,y) ->x+y);
    }

    @Override
    public boolean equals(Object obj){
      if (!(obj instanceof DirectionHeadsignStops)) { return false; }
      DirectionHeadsignStops that = (DirectionHeadsignStops) obj;
      return this.toString().equals(that.toString());
    }

    @Override
    public int hashCode(){
      return this.toString().hashCode();
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

  public static class BeanReferences {
    private Set<AgencyBean> agencies = new LinkedHashSet<>();
    private Set<RouteBean> routes = new LinkedHashSet<>();
    private Set<TripBean> trips = new LinkedHashSet<>();
    private Set<StopBean> stops = new LinkedHashSet<>();
    private Set<StopTimeInstanceBeanExtendedWithStopId> stopTimes = new LinkedHashSet<>();
    public BeanReferences() {
    }
    public Set<AgencyBean> getAgencies() {
      return agencies;
    }
    public Set<RouteBean> getRoutes() {
      return routes;
    }
    public Set<TripBean> getTrips() {
      return trips;
    }
    public Set<StopBean> getStops() {
      return stops;
    }
    public Set<StopTimeInstanceBeanExtendedWithStopId> getStopTimes() {
      return stopTimes;
    }

    public boolean hasAgency(String agencyId) {
      for (AgencyBean bean : agencies) {
        if (bean.getId().equals(agencyId))
          return true;
      }
      return false;
    }

    public boolean hasTrip(AgencyAndId id) {
      for (TripBean bean : trips) {
        if (bean.getId().equals(AgencyAndIdLibrary.convertToString(id)))
          return true;
      }
      return false;
    }

    public boolean hasStop(AgencyAndId id) {
      for (StopBean bean : stops) {
        if (bean.getId().equals(AgencyAndIdLibrary.convertToString(id)))
          return true;
      }
      return false;
    }

    public StopBean getStop(AgencyAndId id) {
      for (StopBean bean : stops) {
        if (bean.getId().equals(AgencyAndIdLibrary.convertToString(id)))
          return bean;
      }
      return null;
    }

    public boolean hasRoute(AgencyAndId id) {
      for (RouteBean bean : routes) {
        if (bean.getId().equals(AgencyAndIdLibrary.convertToString(id)))
          return true;
      }
      return false;
    }

    public RouteBean getRoute(AgencyAndId id) {
      for (RouteBean bean : routes) {
        if (bean.getId().equals(AgencyAndIdLibrary.convertToString(id)))
          return bean;
      }
      return null;
    }

    public AgencyBean getAgencyById(String agencyId) {
      for (AgencyBean bean : agencies) {
        if (bean.getId().equals(agencyId))
          return bean;
      }
      return null;
    }
  }
}
