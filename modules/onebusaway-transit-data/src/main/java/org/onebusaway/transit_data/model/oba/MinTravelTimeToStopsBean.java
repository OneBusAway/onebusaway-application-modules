package org.onebusaway.transit_data.model.oba;

public class MinTravelTimeToStopsBean {
  
  private String _agencyId;

  private String[] _stopIds;

  private double[] _lats;

  private double[] _lons;

  private long[] _transitTimes;

  private double _walkingVelocity;

  public MinTravelTimeToStopsBean(String agencyId, String[] stopIds, double[] lats,
      double[] lons, long[] times, double walkingVelocity) {
    _stopIds = stopIds;
    _lats = lats;
    _lons = lons;
    _transitTimes = times;
    _walkingVelocity = walkingVelocity;
  }
  
  public String getAgencyId() {
    return _agencyId;
  }

  public int getSize() {
    return _stopIds.length;
  }

  public String getStopId(int index) {
    return _stopIds[index];
  }

  public double getStopLat(int i) {
    return _lats[i];
  }

  public double getStopLon(int i) {
    return _lons[i];
  }
  
  public long getTravelTime(int i) {
    return _transitTimes[i];
  }

  public double getWalkingVelocity() {
    return _walkingVelocity;
  }

}
