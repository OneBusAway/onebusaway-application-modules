package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.services.calendar.CalendarService;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.TripBean;
import org.onebusaway.transit_data.model.TripStatusBean;
import org.onebusaway.transit_data_federation.impl.time.StopTimeSearchOperations;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeOp;
import org.onebusaway.transit_data_federation.services.TripPositionService;
import org.onebusaway.transit_data_federation.services.beans.RouteBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripPositionBeanService;
import org.onebusaway.transit_data_federation.services.tripplanner.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeIndex;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripInstanceProxy;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerGraph;

import edu.washington.cs.rse.geospatial.latlon.CoordinateRectangle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TripPositionBeanServiceImpl implements TripPositionBeanService {

  private TripPlannerGraph _graph;

  private CalendarService _calendarService;

  private TripPositionService _tripPositionService;

  private TripBeanService _tripBeanService;
  
  private RouteBeanService _routeBeanService;

  private static final long TIME_WINDOW = 30 * 60 * 1000;

  @Autowired
  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  @Autowired
  public void setCalendarService(CalendarService calendarService) {
    _calendarService = calendarService;
  }

  @Autowired
  public void setTripPositionService(TripPositionService tripPositionService) {
    _tripPositionService = tripPositionService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService){
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setRouteBeanService(RouteBeanService routeBeanService) {
    _routeBeanService = routeBeanService;
  }

  public ListBean<TripStatusBean> getActiveTripForBounds(CoordinateBounds bounds,
      long time) {

    Map<AgencyAndId, List<Date>> serviceIdsAndDates = _calendarService.getServiceDatesWithinRange(
        new Date(time - TIME_WINDOW), new Date(time + TIME_WINDOW));

    long timeFrom = time - TIME_WINDOW;
    long timeTo = time + TIME_WINDOW;

    CoordinateRectangle r = new CoordinateRectangle(bounds.getMinLat(),bounds.getMinLon(),bounds.getMaxLat(),bounds.getMaxLon());
    List<StopEntry> stops = _graph.getStopsByLocation(r);

    Set<TripInstanceProxy> tripInstances = new HashSet<TripInstanceProxy>();

    for (StopEntry stop : stops) {
      StopTimeIndex index = stop.getStopTimes();
      List<StopTimeInstanceProxy> stopTimeInstances = StopTimeSearchOperations.getStopTimeInstancesInRange(
          index, timeFrom, timeTo, StopTimeOp.DEPARTURE, serviceIdsAndDates);
      for (StopTimeInstanceProxy stopTimeInstance : stopTimeInstances) {
        TripEntry trip = stopTimeInstance.getTrip();
        long serviceDate = stopTimeInstance.getServiceDate();
        tripInstances.add(new TripInstanceProxy(trip, serviceDate));
      }
    }

    List<TripStatusBean> results = new ArrayList<TripStatusBean>();

    for (TripInstanceProxy tripInstance : tripInstances) {
      
      CoordinatePoint location = _tripPositionService.getPositionForTripInstance(
          tripInstance, time);
      
      if (location != null
          && bounds.contains(location.getLat(), location.getLon())) {

        TripEntry tripEntry = tripInstance.getTrip();
        
        TripBean trip = _tripBeanService.getTripForId(tripEntry.getId());
        RouteBean route = _routeBeanService.getRouteForId(tripEntry.getRouteCollectionId());

        TripStatusBean result = new TripStatusBean();
        result.setTrip(trip);
        result.setPosition(location);
        result.setRoute(route);
        results.add(result);
      }
    }
    
    return new ListBean<TripStatusBean>(results,false);
  }
}
