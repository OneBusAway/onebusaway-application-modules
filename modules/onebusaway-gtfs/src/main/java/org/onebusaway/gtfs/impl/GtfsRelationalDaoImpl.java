package org.onebusaway.gtfs.impl;

import org.onebusaway.gtfs.csv.schema.BeanWrapper;
import org.onebusaway.gtfs.csv.schema.BeanWrapperFactory;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A in-memory implementation of GtfsRelationalDaoImpl. It's super fast for most
 * methods, but only if you have enough memory to load your entire GTFS into
 * memory.
 * 
 * @author bdferris
 * 
 */
public class GtfsRelationalDaoImpl extends GtfsDaoImpl implements
    GtfsRelationalDao {

  private Map<AgencyAndId, int[]> _arrivalTimeIntervalsByServiceId = null;

  private Map<AgencyAndId, int[]> _departureTimeIntervalsByServiceId = null;

  private Map<Agency, List<Route>> _routesByAgency = null;

  private Map<Trip, List<StopTime>> _stopTimesByTrip = null;

  private Map<Route, List<Trip>> _tripsByRoute;

  private Map<AgencyAndId, List<ShapePoint>> _shapePointsByShapeId;

  @Override
  public int[] getArrivalTimeIntervalForServiceId(AgencyAndId serviceId) {

    if (_arrivalTimeIntervalsByServiceId == null)
      _arrivalTimeIntervalsByServiceId = computeStopTimeInterval(true);

    return _arrivalTimeIntervalsByServiceId.get(serviceId);
  }

  @Override
  public int[] getDepartureTimeIntervalForServiceId(AgencyAndId serviceId) {

    if (_departureTimeIntervalsByServiceId == null)
      _departureTimeIntervalsByServiceId = computeStopTimeInterval(false);

    return _arrivalTimeIntervalsByServiceId.get(serviceId);
  }

  @Override
  public List<Route> getRoutesForAgency(Agency agency) {
    if (_routesByAgency == null)
      _routesByAgency = mapToValueList(getAllRoutes(), "agency", Agency.class);
    return list(_routesByAgency.get(agency));
  }

  @Override
  public List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
    if( _shapePointsByShapeId == null) {
      _shapePointsByShapeId = mapToValueList(getAllShapePoints(), "shapeId", AgencyAndId.class);
      for( List<ShapePoint> shapePoints : _shapePointsByShapeId.values() )
        Collections.sort(shapePoints);
    }
    
    return list(_shapePointsByShapeId.get(shapeId));
  }

  @Override
  public List<StopTime> getStopTimesForTrip(Trip trip) {

    if (_stopTimesByTrip == null){
      _stopTimesByTrip = mapToValueList(getAllStopTimes(), "trip", Trip.class);
      for( List<StopTime> stopTimes : _stopTimesByTrip.values() )
        Collections.sort(stopTimes);
    }

    return list(_stopTimesByTrip.get(trip));
  }

  @Override
  public List<Trip> getTripsForRoute(Route route) {
    if (_tripsByRoute == null)
      _tripsByRoute = mapToValueList(getAllTrips(), "route", Route.class);
    return list(_tripsByRoute.get(route));
  }

  /****
   * Private Methods
   ****/

  private Map<AgencyAndId, int[]> computeStopTimeInterval(boolean useArrival) {

    Map<AgencyAndId, int[]> timeIntervalsByServiceId = new HashMap<AgencyAndId, int[]>();

    for (StopTime stopTime : getAllStopTimes()) {

      AgencyAndId sid = stopTime.getTrip().getServiceId();
      int time = useArrival ? stopTime.getArrivalTime()
          : stopTime.getDepartureTime();

      // A scheduled time of -1 indicates that we have no scheduled time at this stop time
      if( time == -1)
        continue;
      
      int[] interval = timeIntervalsByServiceId.get(sid);
      if (interval == null) {
        interval = new int[] {time, time};
        timeIntervalsByServiceId.put(sid, interval);
      }

      interval[0] = Math.min(interval[0], time);
      interval[1] = Math.max(interval[1], time);
    }

    return timeIntervalsByServiceId;
  }

  private static <T> List<T> list(List<T> list) {
    if (list == null)
      list = new ArrayList<T>();
    return Collections.unmodifiableList(list);
  }

  @SuppressWarnings("unchecked")
  private static <K, V> Map<K, List<V>> mapToValueList(Iterable<V> values,
      String property, Class<K> keyType) {
    return mapToValueCollection(values, property, keyType,
        new ArrayList<V>().getClass());
  }

  @SuppressWarnings("unchecked")
  private static <K, V, C extends Collection<V>, CIMPL extends C> Map<K, C> mapToValueCollection(
      Iterable<V> values, String property, Class<K> keyType,
      Class<CIMPL> collectionType) {

    Map<K, C> byKey = new HashMap<K, C>();
    SimplePropertyQuery query = new SimplePropertyQuery(property);

    for (V value : values) {

      K key = (K) query.invoke(value);
      C valuesForKey = byKey.get(key);
      if (valuesForKey == null) {

        try {
          valuesForKey = collectionType.newInstance();
        } catch (Exception ex) {
          throw new IllegalStateException(
              "error instantiating collection type: " + collectionType, ex);
        }

        byKey.put(key, valuesForKey);
      }
      valuesForKey.add(value);
    }

    return byKey;
  }

  private static final class SimplePropertyQuery {

    private String[] _properties;

    public SimplePropertyQuery(String query) {
      _properties = query.split("\\.");
    }

    public Object invoke(Object value) {
      for (String property : _properties) {
        BeanWrapper wrapper = BeanWrapperFactory.wrap(value);
        value = wrapper.getPropertyValue(property);
      }
      return value;
    }
  }
}
