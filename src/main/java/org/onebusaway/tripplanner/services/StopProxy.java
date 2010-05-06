package org.onebusaway.tripplanner.services;

import com.vividsolutions.jts.geom.Point;

public interface StopProxy {
  
  public String getStopId();

  public Point getStopLocation();
  
  public double getStopLat();
  
  public double getStopLon();
}
