package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.tripplanner.StopEntriesWithValues;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

public interface StopEntry {
  
  public AgencyAndId getId();
  
  public double getStopLat();
  
  public double getStopLon();
  
  public CoordinatePoint getStopLocation();
  
  public StopTimeIndex getStopTimes();

  public StopEntriesWithValues getTransfers();

  public StopEntriesWithValues getPreviousStopsWithMinTimes();

  public StopEntriesWithValues getNextStopsWithMinTimes();
}
