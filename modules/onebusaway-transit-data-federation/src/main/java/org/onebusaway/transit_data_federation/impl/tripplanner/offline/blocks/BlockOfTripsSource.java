package org.onebusaway.transit_data_federation.impl.tripplanner.offline.blocks;

import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.services.ExtendedGtfsRelationalDao;

import java.util.List;

public interface BlockOfTripsSource {

  public List<Trip> getTrips(ExtendedGtfsRelationalDao gtfsDao);

  public List<StopTime> getStopTimes(ExtendedGtfsRelationalDao gtfsDao);
}
