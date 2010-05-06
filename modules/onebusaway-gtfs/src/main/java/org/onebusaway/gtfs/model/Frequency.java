package org.onebusaway.gtfs.model;

public final class Frequency extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;
  
  private int id;
  
  private Trip trip;
  
  private int startTime;
  
  private int endTime;
  
  private int headwaySecs;
  
  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public void setId(Integer id) {
    this.id = id;
  }

  public Trip getTrip() {
    return trip;
  }

  public void setTrip(Trip trip) {
    this.trip = trip;
  }

  public int getStartTime() {
    return startTime;
  }

  public void setStartTime(int startTime) {
    this.startTime = startTime;
  }

  public int getEndTime() {
    return endTime;
  }

  public void setEndTime(int endTime) {
    this.endTime = endTime;
  }

  public int getHeadwaySecs() {
    return headwaySecs;
  }

  public void setHeadwaySecs(int headwaySecs) {
    this.headwaySecs = headwaySecs;
  }

  public String toString() {
    return "<Frequency " + getId() + ">";
  }
}
