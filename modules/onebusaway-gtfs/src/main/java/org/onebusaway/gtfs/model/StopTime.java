package org.onebusaway.gtfs.model;

public final class StopTime extends IdentityBean<Integer> implements
    Comparable<StopTime> {

  private static final long serialVersionUID = 1L;

  private int id;

  private Trip trip;

  private int stopSequence;

  private Stop stop;

  private int arrivalTime;

  private int departureTime;

  private String stopHeadsign;

  private String routeShortName;

  private int pickupType;

  private int dropOffType;

  private double shapeDistTraveled = -1;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Trip getTrip() {
    return trip;
  }

  public void setTrip(Trip trip) {
    this.trip = trip;
  }

  public int getStopSequence() {
    return stopSequence;
  }

  public void setStopSequence(int stopSequence) {
    this.stopSequence = stopSequence;
  }

  public Stop getStop() {
    return stop;
  }

  public void setStop(Stop stop) {
    this.stop = stop;
  }

  /**
   * @return arrival time, in seconds since midnight
   */
  public int getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  /**
   * @return departure time, in seconds since midnight
   */
  public int getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(int departureTime) {
    this.departureTime = departureTime;
  }

  public String getStopHeadsign() {
    return stopHeadsign;
  }

  public void setStopHeadsign(String headSign) {
    this.stopHeadsign = headSign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public int getPickupType() {
    return pickupType;
  }

  public void setPickupType(int pickupType) {
    this.pickupType = pickupType;
  }

  public int getDropOffType() {
    return dropOffType;
  }

  public void setDropOffType(int dropOffType) {
    this.dropOffType = dropOffType;
  }

  public double getShapeDistTraveled() {
    return shapeDistTraveled;
  }

  public void setShapeDistTraveled(double shapeDistTraveled) {
    this.shapeDistTraveled = shapeDistTraveled;
  }

  public int compareTo(StopTime o) {
    return this.stopSequence - o.stopSequence;
  }
 
  @Override
  public String toString() {
    return "<StopTime #"+this.getStopSequence()+" on "+this.getTrip().getId()+" at "+this.getArrivalTime()+","+this.getDepartureTime()+">";
  }
}
