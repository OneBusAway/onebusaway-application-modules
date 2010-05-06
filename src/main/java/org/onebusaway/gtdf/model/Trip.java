package org.onebusaway.gtdf.model;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtdf.serialization.EntityFieldMappingFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "gtdf_trips")
@AccessType("field")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@CsvFields(filename = "trips.txt")
public class Trip extends IdentityBean<String> {

  private static final long serialVersionUID = 1L;

  @Id
  @AccessType("property")
  @CsvField(name = "trip_id")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @CsvField(name = "route_id", mapping = EntityFieldMappingFactory.class)
  private Route route;

  @Column(nullable = false)
  private String serviceId;

  @CsvField(optional = true)
  private String tripHeadsign;

  @CsvField(optional = true)
  private String routeShortName;

  @CsvField(optional = true)
  private String directionId;

  @CsvField(optional = true)
  private String blockId;

  @CsvField(optional = true)
  private String shapeId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route route) {
    this.route = route;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getTripHeadsign() {
    return tripHeadsign;
  }

  public void setTripHeadsign(String tripHeadsign) {
    this.tripHeadsign = tripHeadsign;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public String getBlockId() {
    return blockId;
  }

  public void setBlockId(String blockId) {
    this.blockId = blockId;
  }

  public String getShapeId() {
    return shapeId;
  }

  public void setShapeId(String shapeId) {
    this.shapeId = shapeId;
  }
}
