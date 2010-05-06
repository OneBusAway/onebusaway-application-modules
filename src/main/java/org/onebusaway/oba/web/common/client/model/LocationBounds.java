package org.onebusaway.oba.web.common.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class LocationBounds implements Serializable, IsSerializable {

  private static final long serialVersionUID = 1L;

  private double latMin;
  private double lonMin;
  private double latMax;
  private double lonMax;

  public double getLatMin() {
    return latMin;
  }

  public void setLatMin(double latMin) {
    this.latMin = latMin;
  }

  public double getLonMin() {
    return lonMin;
  }

  public void setLonMin(double lonMin) {
    this.lonMin = lonMin;
  }

  public double getLatMax() {
    return latMax;
  }

  public void setLatMax(double latMax) {
    this.latMax = latMax;
  }

  public double getLonMax() {
    return lonMax;
  }

  public void setLonMax(double lonMax) {
    this.lonMax = lonMax;
  }
}
