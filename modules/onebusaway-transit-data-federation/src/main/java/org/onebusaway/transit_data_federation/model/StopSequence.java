package org.onebusaway.transit_data_federation.model;

import org.onebusaway.container.model.IdentityBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.IndexColumn;

import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "where_stop_sequences")
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
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

  @Embedded
  @AttributeOverrides( {
    @AttributeOverride(name="agencyId",column=@Column(name="shapeId_agencyId")),
    @AttributeOverride(name="id",column=@Column(name="shapeId_id"))
  })
  private AgencyAndId shapeId;

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

  public AgencyAndId getShapeId() {
    return shapeId;
  }

  public void setShapeId(AgencyAndId shapeId) {
    this.shapeId = shapeId;
  }

  @Override
  public String toString() {
    return "StopSequence(id=" + this.id + " directionId=" + this.directionId
        + " trips=" + this.tripCount + ")";
  }
}
