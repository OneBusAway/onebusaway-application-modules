package org.onebusaway.transit_data.model.realtime;

import java.io.Serializable;

import org.onebusaway.transit_data.model.trips.TripStatusBean;

public class CurrentVehicleEstimateBean implements Serializable, Comparable<CurrentVehicleEstimateBean> {

  private static final long serialVersionUID = 1L;

  private double probability;

  private TripStatusBean tripStatus;

  private String debug;

  public double getProbability() {
    return probability;
  }

  public void setProbability(double probability) {
    this.probability = probability;
  }

  public TripStatusBean getTripStatus() {
    return tripStatus;
  }

  public void setTripStatus(TripStatusBean tripStatus) {
    this.tripStatus = tripStatus;
  }

  public String getDebug() {
    return debug;
  }

  public void setDebug(String debug) {
    this.debug = debug;
  }

  @Override
  public int compareTo(CurrentVehicleEstimateBean o) {
    double p1 = this.probability;
    double p2 = o.probability;
    return Double.compare(p2,p1);
  }
}
