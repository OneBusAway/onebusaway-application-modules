package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.StopTimeIndexImpl;
import org.onebusaway.transit_data_federation.impl.tripplanner.offline.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripEntry;

public class MockEntryFactory {

  public static int time(int hour, int minute) {
    return (int) ((hour * 60 + minute) * 60);
  }

  public static StopEntryImpl stop(String id, double lat, double lon) {
    return new StopEntryImpl(aid(id), lat, lon, new StopTimeIndexImpl());
  }

  public static BlockEntryImpl block(String id) {
    BlockEntryImpl block = new BlockEntryImpl();
    block.setId(aid(id));
    return block;
  }

  public static TripEntryImpl trip(String id) {
    TripEntryImpl trip = new TripEntryImpl();
    trip.setId(aid(id));
    return trip;
  }
  
  public static TripEntryImpl trip(String id, String serviceId) {
    TripEntryImpl trip = new TripEntryImpl();
    trip.setId(aid(id));
    trip.setServiceId(aid(serviceId));
    return trip;
  }

  public static TripEntryImpl trip(String id, double distanceAlongBlock) {
    TripEntryImpl trip = trip(id);
    trip.setDistanceAlongBlock(distanceAlongBlock);
    return trip;
  }

  public static void linkBlockTrips(BlockEntryImpl block,
      TripEntryImpl... trips) {
    List<TripEntry> tripEntries = new ArrayList<TripEntry>();
    for (int i = 0; i < trips.length; i++) {
      TripEntryImpl trip = trips[i];
      trip.setBlock(block);
      tripEntries.add(trip);
      if (i > 0) {
        TripEntryImpl prev = trips[i - 1];
        prev.setNextTrip(trip);
        trip.setPrevTrip(prev);
      }
    }
    block.setTrips(tripEntries);
  }

  public static void addStopTime(TripEntryImpl trip, StopTimeEntryImpl stopTime) {
    
    BlockEntryImpl block = trip.getBlock();
    List<StopTimeEntry> stopTimes = block.getStopTimes();
    
    if(stopTimes == null) {
      stopTimes = new ArrayList<StopTimeEntry>();
      block.setStopTimes(stopTimes);
    }
    
    stopTimes.add(stopTime);
    int toIndex = stopTimes.size();
    int fromIndex = toIndex - 1;
    while (fromIndex > 0 && stopTimes.get(fromIndex - 1).getTrip().equals(trip))
      fromIndex--;
    trip.setStopTimeIndices(fromIndex, toIndex);
    stopTime.setTrip(trip);
  }

  public static StopTimeEntryImpl stopTime(int id, StopEntryImpl stop,
      TripEntryImpl trip, int arrivalTime, int departureTime,
      double shapeDistTraveled) {
    StopTimeEntryImpl stopTime = new StopTimeEntryImpl();
    stopTime.setId(id);
    stopTime.setStop(stop);
    stopTime.setTrip(trip);
    stopTime.setArrivalTime(arrivalTime);
    stopTime.setDepartureTime(departureTime);
    stopTime.setShapeDistTraveled(shapeDistTraveled);
    return stopTime;
  }

  public static AgencyAndId aid(String id) {
    return new AgencyAndId("1", id);
  }

  public static ShapePoints shapePointsFromLatLons(String id, double... values) {

    if (values.length % 2 != 0)
      throw new IllegalStateException();

    int n = values.length / 2;

    double[] lats = new double[n];
    double[] lons = new double[n];
    double[] distances = new double[n];

    double distance = 0;

    for (int i = 0; i < n; i++) {
      lats[i] = values[i * 2];
      lons[i] = values[i * 2 + 1];
      if (i > 0) {
        distance += SphericalGeometryLibrary.distance(lats[i - 1], lons[i - 1],
            lats[i], lons[i]);
      }
      distances[i] = distance;
    }

    return shapePoints(id, lats, lons, distances);
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
