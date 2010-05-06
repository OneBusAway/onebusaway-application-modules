package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeIndexImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public class MockEntryFactory {

  public static StopEntryImpl stop(String id, double lat, double lon) {
    return new StopEntryImpl(aid(id), lat, lon, new StopTimeIndexImpl());
  }

  public static TripEntryImpl trip(String id) {
    TripEntryImpl trip = new TripEntryImpl();
    trip.setId(aid(id));
    return trip;
  }

  public static StopTimeEntryImpl stopTime(int id, StopEntryImpl stop,
      TripEntryImpl trip, int arrivalTime, int departureTime) {
    StopTimeEntryImpl stopTime = new StopTimeEntryImpl();
    stopTime.setId(id);
    stopTime.setStop(stop);
    stopTime.setTrip(trip);
    stopTime.setArrivalTime(arrivalTime);
    stopTime.setDepartureTime(departureTime);
    return stopTime;
  }

  public static AgencyAndId aid(String id) {
    return new AgencyAndId("1", id);
  }

  public static ShapePoints shapePoints(String id, double[] lats,
      double[] lons, double[] distTraveled) {
    ShapePoints shapePoints = new ShapePoints();
    shapePoints.setShapeId(aid(id));
    shapePoints.setLats(lats);
    shapePoints.setLons(lons);
    shapePoints.setDistTraveled(distTraveled);
    return shapePoints;
  }
}
