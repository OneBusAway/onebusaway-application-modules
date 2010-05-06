package org.onebusaway.where.offline;

import edu.washington.cs.rse.collections.FactoryMap;

import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;
import org.onebusaway.gtdf.model.Trip;
import org.onebusaway.gtdf.services.GtdfDao;
import org.onebusaway.where.model.StopSequence;
import org.onebusaway.where.services.WhereDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class StopSequencesTask implements Runnable {

  private static final String NO_SHAPE_ID = StopSequencesTask.class.getName()
      + ".noShapeId";

  private static final String NO_DIRECTION_ID = StopSequencesTask.class.getName()
      + ".noDirectionId";

  @Autowired
  private GtdfDao _gtdfDao;

  @Autowired
  private WhereDao _whereDao;

  public void run() {

    List<Route> routes = getRoutes();
    int index = 0;

    for (Route route : routes) {

      System.out.println("route=" + route + " " + (index++) + "/"
          + routes.size());

      List<Trip> trips = _gtdfDao.getTripsByRoute(route);

      Map<StopSequenceKey, List<Trip>> tripsByStopSequenceKey = new FactoryMap<StopSequenceKey, List<Trip>>(
          new ArrayList<Trip>());

      for (Trip trip : trips) {
        String directionId = trip.getDirectionId();
        if (directionId == null)
          directionId = NO_DIRECTION_ID;
        String shapeId = trip.getShapeId();
        if (shapeId == null)
          shapeId = NO_SHAPE_ID;
        List<StopTime> stopTimes = _gtdfDao.getStopTimesByTrip(trip);
        Collections.sort(stopTimes);
        List<Stop> stops = getStopTimesAsStops(stopTimes);

        StopSequenceKey key = new StopSequenceKey(stops, directionId, shapeId);
        tripsByStopSequenceKey.get(key).add(trip);
      }

      for (Map.Entry<StopSequenceKey, List<Trip>> entry : tripsByStopSequenceKey.entrySet()) {
        StopSequenceKey key = entry.getKey();
        StopSequence ss = new StopSequence();
        ss.setRoute(route);
        ss.setStops(key.getStops());
        if (!key.getDirectionId().equals(NO_DIRECTION_ID))
          ss.setDirectionId(key.getDirectionId());
        if (!key.getShapeId().equals(NO_SHAPE_ID))
          ss.setShapeId(key.getShapeId());
        ss.setTrips(entry.getValue());
        ss.setTripCount(entry.getValue().size());
        _whereDao.save(ss);
      }
    }
  }

  private List<Route> getRoutes() {
    if (true)
      return _gtdfDao.getAllRoutes();
    List<Route> routes = new ArrayList<Route>();
    routes.add(_gtdfDao.getRouteByShortName("23"));
    return routes;
  }

  private List<Stop> getStopTimesAsStops(List<StopTime> stopTimes) {
    List<Stop> stops = new ArrayList<Stop>(stopTimes.size());
    for (StopTime st : stopTimes)
      stops.add(st.getStop());
    return stops;
  }

  private static class StopSequenceKey {
    private List<Stop> stops;
    private String directionId;
    private String shapeId;

    public StopSequenceKey(List<Stop> stops, String directionId, String shapeId) {
      this.stops = stops;
      this.directionId = directionId;
      this.shapeId = shapeId;
    }

    public List<Stop> getStops() {
      return stops;
    }

    public String getDirectionId() {
      return directionId;
    }

    public String getShapeId() {
      return shapeId;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof StopSequenceKey))
        return false;

      StopSequenceKey key = (StopSequenceKey) obj;
      return this.stops.equals(key.stops)
          && this.directionId.equals(key.directionId)
          && this.shapeId.equals(key.shapeId);
    }

    @Override
    public int hashCode() {
      return stops.hashCode() + directionId.hashCode() + shapeId.hashCode();
    }
  }
}
