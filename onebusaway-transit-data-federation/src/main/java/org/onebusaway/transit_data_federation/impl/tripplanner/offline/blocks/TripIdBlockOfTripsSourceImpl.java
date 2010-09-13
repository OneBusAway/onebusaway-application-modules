package org.onebusaway.transit_data_federation.impl.tripplanner.offline.blocks;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;

import java.util.Arrays;
import java.util.List;

public class TripIdBlockOfTripsSourceImpl implements BlockOfTripsSource {

  private AgencyAndId _tripId;

  public TripIdBlockOfTripsSourceImpl(AgencyAndId tripId) {
    _tripId = tripId;
  }
  
  public AgencyAndId getTripId() {
    return _tripId;
  }

  public List<Trip> getTrips(ExtendedGtfsRelationalDao gtfsDao) {
    return Arrays.asList(gtfsDao.getTripForId(_tripId));
  }

  public List<StopTime> getStopTimes(ExtendedGtfsRelationalDao gtfsDao) {
    Trip trip = gtfsDao.getTripForId(_tripId);
    return gtfsDao.getStopTimesForTrip(trip);
  }
  
  @Override
  public String toString() {
    return "TripIdBlockOfTripsSourceImpl(tripId=" + _tripId + ")";
  }
}
