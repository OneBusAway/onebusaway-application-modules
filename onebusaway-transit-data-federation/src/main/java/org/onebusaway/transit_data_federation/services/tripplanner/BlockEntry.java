package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.List;

public interface BlockEntry {

  public AgencyAndId getId();

  public List<TripEntry> getTrips();

  public List<StopTimeEntry> getStopTimes();

  /**
   * @return distance, in meters
   */
  public double getTotalBlockDistance();
}
