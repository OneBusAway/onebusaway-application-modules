package org.onebusaway.transit_data_federation.model.narrative;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

public final class StopTimeNarrative implements Serializable {

  private static final long serialVersionUID = 1L;

  private final int id;

  private final AgencyAndId tripId;

  private final int stopSequence;

  private final AgencyAndId stopId;

  private final int arrivalTime;

  private final int departureTime;

  private final String stopHeadsign;

  private final String routeShortName;

  private final int pickupType;

  private final int dropOffType;

  private final double shapeDistTraveled;

  public static Builder builder() {
    return new Builder();
  }
  
  private StopTimeNarrative(Builder builder) {
    this.id = builder.id;
    this.tripId = builder.tripId;
    this.stopSequence = builder.stopSequence;
    this.stopId = builder.stopId;
    this.arrivalTime = builder.arrivalTime;
    this.departureTime = builder.departureTime;
    this.stopHeadsign = builder.stopHeadsign;
    this.routeShortName = builder.routeShortName;
    this.pickupType = builder.pickupType;
    this.dropOffType = builder.dropOffType;
    this.shapeDistTraveled = builder.shapeDistTraveled;
  }

  public Integer getId() {
    return id;
  }

  public AgencyAndId getTripId() {
    return tripId;
  }

  public int getStopSequence() {
    return stopSequence;
  }

  public AgencyAndId getStopId() {
    return stopId;
  }

  /**
   * @return arrival time, in seconds since midnight
   */
  public int getArrivalTime() {
    return arrivalTime;
  }

  /**
   * @return departure time, in seconds since midnight
   */
  public int getDepartureTime() {
    return departureTime;
  }

  public String getStopHeadsign() {
    return stopHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public int getPickupType() {
    return pickupType;
  }

  public int getDropOffType() {
    return dropOffType;
  }

  public double getShapeDistTraveled() {
    return shapeDistTraveled;
  }

  public static class Builder {

    private int id;

    private AgencyAndId tripId;

    private int stopSequence;

    private AgencyAndId stopId;

    private int arrivalTime;

    private int departureTime;

    private String stopHeadsign;

    private String routeShortName;

    private int pickupType;

    private int dropOffType;

    private double shapeDistTraveled = -1;

    public StopTimeNarrative create() {
      return new StopTimeNarrative(this);
    }

    public Builder setId(int id) {
      this.id = id;
      return this;
    }

    public Builder setTripId(AgencyAndId tripId) {
      this.tripId = tripId;
      return this;
    }

    public Builder setStopSequence(int stopSequence) {
      this.stopSequence = stopSequence;
      return this;
    }

    public Builder setStopId(AgencyAndId stopId) {
      this.stopId = stopId;
      return this;
    }

    public Builder setArrivalTime(int arrivalTime) {
      this.arrivalTime = arrivalTime;
      return this;
    }

    public Builder setDepartureTime(int departureTime) {
      this.departureTime = departureTime;
      return this;
    }

    public Builder setStopHeadsign(String stopHeadsign) {
      this.stopHeadsign = stopHeadsign;
      return this;
    }

    public Builder setRouteShortName(String routeShortName) {
      this.routeShortName = routeShortName;
      return this;
    }

    public Builder setPickupType(int pickupType) {
      this.pickupType = pickupType;
      return this;
    }

    public Builder setDropOffType(int dropOffType) {
      this.dropOffType = dropOffType;
      return this;
    }

    public Builder setShapeDistTraveled(double shapeDistTraveled) {
      this.shapeDistTraveled = shapeDistTraveled;
      return this;
    }
  }
}
