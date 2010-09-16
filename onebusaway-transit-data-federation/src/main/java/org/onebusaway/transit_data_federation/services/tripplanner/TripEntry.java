package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.List;

public interface TripEntry {

  public AgencyAndId getId();

  public AgencyAndId getRouteId();

  public AgencyAndId getRouteCollectionId();

  public BlockEntry getBlock();

  public AgencyAndId getServiceId();
  
  public AgencyAndId getShapeId();

  public List<StopTimeEntry> getStopTimes();

  /**
   * This is the index into the array of all stop times for a given block (as
   * returned by {@link BlockEntry#getStopTimes()} where the stop times for this
   * particular trip can be found in the block.
   * 
   * @return an index
   */
  public int getBlockStopTimeSequenceOffset();

  public double getDistanceAlongBlock();

  public TripEntry getPrevTrip();

  public TripEntry getNextTrip();
}
