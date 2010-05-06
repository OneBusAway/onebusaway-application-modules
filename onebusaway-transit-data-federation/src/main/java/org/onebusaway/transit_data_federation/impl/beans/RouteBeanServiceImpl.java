package org.onebusaway.transit_data_federation.impl.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.NameBean;
import org.onebusaway.transit_data.model.NameBeanTypes;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data_federation.impl.StopGraphComparator;
import org.onebusaway.transit_data_federation.model.RouteCollection;
import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.model.StopSequenceBlock;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;
import org.onebusaway.transit_data_federation.services.RouteService;
import org.onebusaway.transit_data_federation.services.StopSequenceBlocksService;
import org.onebusaway.transit_data_federation.services.StopSequencesService;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.beans.AgencyBeanService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.ShapeBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.washington.cs.rse.collections.graph.DirectedGraph;

@Component
class RouteBeanServiceImpl implements RouteBeanService {

  private ExtendedGtfsRelationalDao _gtfsDao;

  private TransitDataFederationDao _transitDataFederationDao;

  private AgencyBeanService _agencyBeanService;

  private StopBeanService _stopBeanService;

  private ShapeBeanService _shapeBeanService;

  private RouteService _routeService;

  private StopSequencesService _stopSequencesService;

  private StopSequenceBlocksService _stopSequenceBlocksService;

  @Autowired
  public void setGtfsDao(ExtendedGtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setTransitDataFederationDao(
      TransitDataFederationDao transitDataFederationDao) {
    _transitDataFederationDao = transitDataFederationDao;
  }

  @Autowired
  public void setAgencyBeanService(AgencyBeanService agencyBeanService) {
    _agencyBeanService = agencyBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Autowired
  public void setShapeBeanService(ShapeBeanService shapeBeanService) {
    _shapeBeanService = shapeBeanService;
  }

  @Autowired
  public void setRouteService(RouteService routeService) {
    _routeService = routeService;
  }

  @Autowired
  public void setStopSequencesLibrary(StopSequencesService service) {
    _stopSequencesService = service;
  }

  @Autowired
  public void setStopSequencesBlocksService(
      StopSequenceBlocksService stopSequenceBlocksService) {
    _stopSequenceBlocksService = stopSequenceBlocksService;
  }

  @Cacheable
  public RouteBean getRouteForId(AgencyAndId id) {

    RouteCollection rc = _transitDataFederationDao.getRouteCollectionForId(id);

    if (rc == null)
      return null;

    RouteBean.Builder bean = RouteBean.builder();
    bean.setId(ApplicationBeanLibrary.getId(id));
    bean.setShortName(rc.getShortName());
    bean.setLongName(rc.getLongName());
    bean.setColor(rc.getColor());
    bean.setDescription(rc.getDescription());
    bean.setTextColor(rc.getTextColor());
    bean.setType(rc.getType());
    bean.setUrl(rc.getUrl());

    AgencyBean agency = _agencyBeanService.getAgencyForId(id.getAgencyId());
    bean.setAgency(agency);

    return bean.create();
  }

  @Cacheable
  @Transactional
  public StopsForRouteBean getStopsForRoute(AgencyAndId routeId) {

    RouteCollection routeCollection = _transitDataFederationDao.getRouteCollectionForId(routeId);

    if (routeCollection == null)
      return null;

    return go(routeCollection);
  }

  /****
   * Private Methods
   ****/

  private List<StopBean> getStopBeansForRoute(AgencyAndId routeId) {

    Collection<AgencyAndId> stopIds = _routeService.getStopsForRouteCollection(routeId);
    List<StopBean> stops = new ArrayList<StopBean>();

    for (AgencyAndId stopId : stopIds) {
      StopBean stop = _stopBeanService.getStopForId(stopId);
      stops.add(stop);
    }

    return stops;
  }

  private List<EncodedPolylineBean> getEncodedPolylinesForRoute(
      RouteCollection routeCollection) {

    List<AgencyAndId> shapeIds = _gtfsDao.getShapePointIdsForRoutes(routeCollection.getRoutes());
    return _shapeBeanService.getMergedPolylinesForShapeIds(shapeIds);
  }

  private StopsForRouteBean go(RouteCollection routeCollection) {

    StopsForRouteBean result = new StopsForRouteBean();

    result.setStops(getStopBeansForRoute(routeCollection.getId()));

    result.setPolylines(getEncodedPolylinesForRoute(routeCollection));

    StopGroupingBean directionGrouping = new StopGroupingBean();
    directionGrouping.setType(TransitDataConstants.STOP_GROUPING_TYPE_DIRECTION);
    List<StopGroupBean> directionGroups = new ArrayList<StopGroupBean>();
    directionGrouping.setStopGroups(directionGroups);
    directionGrouping.setOrdered(true);
    result.addGrouping(directionGrouping);

    Set<AgencyAndId> stopIds = new HashSet<AgencyAndId>();
    List<Route> routes = routeCollection.getRoutes();

    Map<Trip, List<StopTime>> stopTimesByTrip = new HashMap<Trip, List<StopTime>>();

    for (Route route : routes) {
      List<Trip> trips = _gtfsDao.getTripsForRoute(route);
      for (Trip trip : trips) {
        List<StopTime> stopTimes = _gtfsDao.getStopTimesForTrip(trip);
        stopTimesByTrip.put(trip, stopTimes);
        for (StopTime stopTime : stopTimes)
          stopIds.add(stopTime.getStop().getId());
      }
    }

    List<StopSequence> sequences = _stopSequencesService.getStopSequencesForTrips(stopTimesByTrip);

    List<StopSequenceBlock> blocks = _stopSequenceBlocksService.getStopSequencesAsBlocks(sequences);

    for (StopSequenceBlock block : blocks) {

      NameBean name = new NameBean(NameBeanTypes.DESTINATION,
          block.getDescription());

      List<Stop> stops = getStopsInOrder(block);
      List<String> groupStopIds = new ArrayList<String>();
      for (Stop stop : stops)
        groupStopIds.add(ApplicationBeanLibrary.getId(stop.getId()));

      Set<AgencyAndId> shapeIds = getShapeIdsForStopSequenceBlock(block);
      List<EncodedPolylineBean> polylines = _shapeBeanService.getMergedPolylinesForShapeIds(shapeIds);

      StopGroupBean group = new StopGroupBean();
      group.setName(name);

      group.setStopIds(groupStopIds);
      group.setPolylines(polylines);
      directionGroups.add(group);
    }

    sortResult(result);

    return result;
  }

  private List<Stop> getStopsInOrder(StopSequenceBlock block) {
    DirectedGraph<Stop> graph = new DirectedGraph<Stop>();
    for (StopSequence sequence : block.getStopSequences()) {
      Stop prev = null;
      for (Stop stop : sequence.getStops()) {
        if (prev != null) {
          // We do this to avoid cycles
          if (!graph.isConnected(stop, prev))
            graph.addEdge(prev, stop);
        }
        prev = stop;
      }
    }

    StopGraphComparator c = new StopGraphComparator(graph);
    return graph.getTopologicalSort(c);
  }

  private Set<AgencyAndId> getShapeIdsForStopSequenceBlock(
      StopSequenceBlock block) {
    Set<AgencyAndId> shapeIds = new HashSet<AgencyAndId>();
    for (StopSequence sequence : block.getStopSequences()) {
      for (Trip trip : sequence.getTrips()) {
        AgencyAndId shapeId = trip.getShapeId();
        if (shapeId != null && shapeId.hasValues())
          shapeIds.add(shapeId);
      }
    }
    return shapeIds;
  }

  private void sortResult(StopsForRouteBean result) {

    Collections.sort(result.getStops(), new StopBeanIdComparator());

    Collections.sort(result.getStopGroupings(),
        new Comparator<StopGroupingBean>() {
          public int compare(StopGroupingBean o1, StopGroupingBean o2) {
            return o1.getType().compareTo(o2.getType());
          }
        });

    for (StopGroupingBean grouping : result.getStopGroupings()) {
      Collections.sort(grouping.getStopGroups(),
          new Comparator<StopGroupBean>() {

            public int compare(StopGroupBean o1, StopGroupBean o2) {
              return getName(o1).compareTo(getName(o2));
            }

            private String getName(StopGroupBean bean) {
              StringBuilder b = new StringBuilder();
              for (String name : bean.getName().getNames())
                b.append(name);
              return b.toString();
            }
          });
    }
  }

}
