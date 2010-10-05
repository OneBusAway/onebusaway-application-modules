package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.calendar.LocalizedServiceId;

import java.util.List;

public interface TripEntry {

  public AgencyAndId getId();

  public AgencyAndId getRouteId();

  public AgencyAndId getRouteCollectionId();

  public BlockEntry getBlock();

  public LocalizedServiceId getServiceId();

  public AgencyAndId getShapeId();

  public List<StopTimeEntry> getStopTimes();

  /**
   * @return distance, in meters
   */
  public double getTotalTripDistance();
}
