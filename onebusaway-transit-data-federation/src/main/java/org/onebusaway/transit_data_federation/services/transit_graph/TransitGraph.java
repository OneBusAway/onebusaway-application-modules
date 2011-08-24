package org.onebusaway.transit_data_federation.services.transit_graph;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;

public interface TransitGraph {

  public Iterable<StopEntry> getAllStops();

  public Iterable<TripEntry> getAllTrips();

  public Iterable<BlockEntry> getAllBlocks();

  public BlockEntry getBlockEntryForId(AgencyAndId blockId);

  public TripEntry getTripEntryForId(AgencyAndId id);
  
  public StopEntry getStopEntryForId(AgencyAndId id);

  public Iterable<StopEntry> getStopsByLocation(CoordinateBounds bounds);
}