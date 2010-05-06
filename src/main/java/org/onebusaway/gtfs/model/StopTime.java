package org.onebusaway.gtfs.model;

import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtfs.serialization.EntityFieldMappingFactory;

import org.hibernate.annotations.AccessType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "gtfs_stop_times")
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
@CsvFields(filename = "stop_times.txt")
public class StopTime extends IdentityBean<Integer> implements Comparable<StopTime> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  @CsvField(ignore = true)
  private int id;

  @ManyToOne(optional = false)
  @CsvField(name = "trip_id", mapping = EntityFieldMappingFactory.class)
  private Trip trip;

  private int stopSequence;

  @ManyToOne(optional = false)
  @CsvField(name = "stop_id", mapping = EntityFieldMappingFactory.class)
  private Stop stop;

  @CsvField(mapping = org.onebusaway.gtfs.serialization.StopTimeFieldMappingFactory.class)
  private int arrivalTime;

  @CsvField(mapping = org.onebusaway.gtfs.serialization.StopTimeFieldMappingFactory.class)
  private int departureTime;

  @CsvField(optional = true)
  private String stopHeadsign;

  @CsvField(optional = true)
  private String routeShortName;

  @CsvField(optional = true)
  private int pickupType;

  @CsvField(optional = true)
  private int dropOffType;

  @CsvField(optional = true)
  private double shapeDistanceTraveled;

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

  public double getShapeDistanceTraveled() {
    return shapeDistanceTraveled;
  }

  public void setShapeDistanceTraveled(double shapeDistanceTraveled) {
    this.shapeDistanceTraveled = shapeDistanceTraveled;
  }

  public int compareTo(StopTime o) {
    return this.departureTime == o.departureTime ? 0 : (this.departureTime < o.departureTime ? -1 : 1);
  }
}
