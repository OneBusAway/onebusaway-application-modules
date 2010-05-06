package org.onebusaway.tcip.model;

import org.onebusaway.lrms.model.GeoLocation;
import org.onebusaway.tcip.impl.DurationConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import java.util.Date;
import java.util.List;

@XStreamAlias("arrival-estimate")
public class PISchedAdherenceCountdown {

  private CPTStoppointIden stoppoint;

  private SCHRouteIden route;

  private String routeDirection;

  private String destination;

  @XStreamAlias("gate-bay")
  private long gateBay;

  private SCHTripIden trip;

  private CPTVehicleIden vehicle;

  @XStreamConverter(value = DurationConverter.class)
  private long nextArrivalCountdown;

  @XStreamConverter(value = DurationConverter.class)
  private long tolerance;

  @XStreamAlias("estimated-departure")
  private Date estimatedDeparture;

  private String comment;

  @XStreamAlias("available-seats")
  private short availableSeats;

  private GeoLocation nextArrivalCurrentLocation;

  private List<PIServiceBulletin> bulletins;

  private String footnote;

  public CPTStoppointIden getStoppoint() {
    return stoppoint;
  }

  public void setStoppoint(CPTStoppointIden stoppoint) {
    this.stoppoint = stoppoint;
  }

  public SCHRouteIden getRoute() {
    return route;
  }

  public void setRoute(SCHRouteIden route) {
    this.route = route;
  }

  public String getRouteDirection() {
    return routeDirection;
  }

  public void setRouteDirection(String routeDirection) {
    this.routeDirection = routeDirection;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public long getGateBay() {
    return gateBay;
  }

  public void setGateBay(long gateBay) {
    this.gateBay = gateBay;
  }

  public SCHTripIden getTrip() {
    return trip;
  }

  public void setTrip(SCHTripIden trip) {
    this.trip = trip;
  }

  public CPTVehicleIden getVehicle() {
    return vehicle;
  }

  public void setVehicle(CPTVehicleIden vehicle) {
    this.vehicle = vehicle;
  }

  public long getNextArrivalCountdown() {
    return nextArrivalCountdown;
  }

  public void setNextArrivalCountdown(long nextArrivalCountdown) {
    this.nextArrivalCountdown = nextArrivalCountdown;
  }

  public long getTolerance() {
    return tolerance;
  }

  public void setTolerance(long tolerance) {
    this.tolerance = tolerance;
  }

  public Date getEstimatedDeparture() {
    return estimatedDeparture;
  }

  public void setEstimatedDeparture(Date estimatedDeparture) {
    this.estimatedDeparture = estimatedDeparture;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public short getAvailableSeats() {
    return availableSeats;
  }

  public void setAvailableSeats(short availableSeats) {
    this.availableSeats = availableSeats;
  }

  public GeoLocation getNextArrivalCurrentLocation() {
    return nextArrivalCurrentLocation;
  }

  public void setNextArrivalCurrentLocation(
      GeoLocation nextArrivalCurrentLocation) {
    this.nextArrivalCurrentLocation = nextArrivalCurrentLocation;
  }

  public List<PIServiceBulletin> getBulletins() {
    return bulletins;
  }

  public void setBulletins(List<PIServiceBulletin> bulletins) {
    this.bulletins = bulletins;
  }

  public String getFootnote() {
    return footnote;
  }

  public void setFootnote(String footnote) {
    this.footnote = footnote;
  }
}
