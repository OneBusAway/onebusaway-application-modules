package org.onebusaway.tripplanner.offline;

import org.onebusaway.common.spring.CacheableKey;
import org.onebusaway.tripplanner.services.StopProxy;

import com.vividsolutions.jts.geom.Point;

import java.io.Serializable;

@CacheableKey(keyFactory = StopProxyImplKeyFactoryImpl.class)
class StopProxyImpl implements StopProxy, Serializable {

  private static final long serialVersionUID = 1L;

  private final String _stopId;

  private final Point _location;

  private final double _lat;

  private final double _lon;

  public StopProxyImpl(String stopId, Point location, double lat, double lon) {
    _stopId = stopId;
    _location = location;
    _lat = lat;
    _lon = lon;
  }

  public String getStopId() {
    return _stopId;
  }

  public Point getStopLocation() {
    return _location;
  }

  public double getStopLat() {
    return _lat;
  }

  public double getStopLon() {
    return _lon;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopProxyImpl))
      return false;
    StopProxyImpl stop = (StopProxyImpl) obj;
    return _stopId.equals(stop.getStopId());
  }

  @Override
  public int hashCode() {
    return _stopId.hashCode();
  }
}
