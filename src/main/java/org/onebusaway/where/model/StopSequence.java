package org.onebusaway.where.model;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.IndexColumn;
import org.onebusaway.gtdf.model.IdentityBean;
import org.onebusaway.gtdf.model.Route;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.Trip;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "where_stop_sequences")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@AccessType("field")
public class StopSequence extends IdentityBean<Integer> {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  @AccessType("property")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Route route;

  @ManyToMany(fetch = FetchType.LAZY)
  @IndexColumn(name = "sequence", base = 1)
  private List<Stop> stops;

  @ManyToMany(fetch = FetchType.LAZY)
  private List<Trip> trips;

  private int tripCount;

  private String directionId;

  private String shapeId;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public List<Stop> getStops() {
    return this.stops;
  }

  public void setStops(List<Stop> stops) {
    this.stops = stops;
  }

  public List<Trip> getTrips() {
    return this.trips;
  }

  public void setTrips(List<Trip> trips) {
    this.trips = trips;
  }

  public int getTripCount() {
    return tripCount;
  }

  public void setTripCount(int tripCount) {
    this.tripCount = tripCount;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public String getShapeId() {
    return shapeId;
  }

  public void setShapeId(String shapeId) {
    this.shapeId = shapeId;
  }

  @Override
  public String toString() {
    return "StopSequence(id=" + this.id + " directionId=" + this.directionId
        + " trips=" + this.tripCount + ")";
  }
}
