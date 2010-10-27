package org.onebusaway.transit_data_federation.services.tripplanner;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.tripplanner.StopEntriesWithValues;

public interface StopEntry {

  public AgencyAndId getId();

  public double getStopLat();

  public double getStopLon();

  public CoordinatePoint getStopLocation();

  public StopEntriesWithValues getPreviousStopsWithMinTimes();

  public StopEntriesWithValues getNextStopsWithMinTimes();
}
