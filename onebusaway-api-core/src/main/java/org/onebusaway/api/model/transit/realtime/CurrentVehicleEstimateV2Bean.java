package org.onebusaway.api.model.transit.realtime;

import java.io.Serializable;

import org.onebusaway.api.model.transit.TripStatusV2Bean;

public class CurrentVehicleEstimateV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private double probability;

  private TripStatusV2Bean tripStatus;
  
  private String debug;

  public double getProbability() {
    return probability;
  }

  public void setProbability(double probability) {
    this.probability = probability;
  }

  public TripStatusV2Bean getTripStatus() {
    return tripStatus;
  }

  public void setTripStatus(TripStatusV2Bean tripStatus) {
    this.tripStatus = tripStatus;
  }

  public String getDebug() {
    return debug;
  }

  public void setDebug(String debug) {
    this.debug = debug;
  }
}
