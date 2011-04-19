package org.onebusaway.transit_data_federation.services.transit_graph;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;

public interface StopEntry {

  public AgencyAndId getId();

  public double getStopLat();

  public double getStopLon();

  public CoordinatePoint getStopLocation();
}
