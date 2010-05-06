package org.onebusaway.transit_data_federation.model.narrative;

import java.io.Serializable;

public final class StopTimeNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String stopHeadsign;

  private final String routeShortName;

  private final double shapeDistTraveled;

  public static Builder builder() {
    return new Builder();
  }

  private StopTimeNarrative(Builder builder) {
    this.stopHeadsign = builder.stopHeadsign;
    this.routeShortName = builder.routeShortName;
    this.shapeDistTraveled = builder.shapeDistTraveled;
  }

  public String getStopHeadsign() {
    return stopHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public double getShapeDistTraveled() {
    return shapeDistTraveled;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((routeShortName == null) ? 0 : routeShortName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(shapeDistTraveled);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result
        + ((stopHeadsign == null) ? 0 : stopHeadsign.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StopTimeNarrative other = (StopTimeNarrative) obj;
    if (routeShortName == null) {
      if (other.routeShortName != null)
        return false;
    } else if (!routeShortName.equals(other.routeShortName))
      return false;
    if (Double.doubleToLongBits(shapeDistTraveled) != Double.doubleToLongBits(other.shapeDistTraveled))
      return false;
    if (stopHeadsign == null) {
      if (other.stopHeadsign != null)
        return false;
    } else if (!stopHeadsign.equals(other.stopHeadsign))
      return false;
    return true;
  }

  public static class Builder {

    private String stopHeadsign;

    private String routeShortName;

    private double shapeDistTraveled = -1;

    public StopTimeNarrative create() {
      return new StopTimeNarrative(this);
    }

    public Builder setStopHeadsign(String stopHeadsign) {
      this.stopHeadsign = stopHeadsign;
      return this;
    }

    public Builder setRouteShortName(String routeShortName) {
      this.routeShortName = routeShortName;
      return this;
    }

    public Builder setShapeDistTraveled(double shapeDistTraveled) {
      this.shapeDistTraveled = shapeDistTraveled;
      return this;
    }
  }
}
