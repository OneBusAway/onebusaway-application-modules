package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.tripplanner.StopEntriesWithValues;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;

public interface StopEntry {
  
  public AgencyAndId getId();
  
  public double getStopLat();
  
  public double getStopLon();
  
  public CoordinatePoint getStopLocation();
  
  public List<BlockStopTimeIndex> getStopTimeIndices();

  public StopEntriesWithValues getTransfers();

  public StopEntriesWithValues getPreviousStopsWithMinTimes();

  public StopEntriesWithValues getNextStopsWithMinTimes();
}
