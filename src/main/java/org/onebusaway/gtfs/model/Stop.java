package org.onebusaway.gtfs.model;

import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtfs.serialization.LocationFieldMappingFactory;

import com.vividsolutions.jts.geom.Point;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gtfs_stops")
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@CsvFields(filename = "stops.txt", prefix = "stop_")
public class Stop extends IdentityBean<String> {

  private static final long serialVersionUID = 1L;

  @Id
  @AccessType("property")
  private String id;

  @CsvField(optional = true)
  private String code;

  private String name;

  // "desc" is a mysql reserved keyword
  @Column(name = "description")
  @CsvField(optional = true)
  private String desc;

  private double lat;

  private double lon;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY", nullable = false)
  @Index(name = "location")
  @CsvField(mapping = LocationFieldMappingFactory.class)
  private Point location;

  @CsvField(name = "zone_id", optional = true)
  private int zoneId;

  @CsvField(optional = true)
  private String url;

  @CsvField(name = "location_type", optional = true)
  private int locationType;

  @CsvField(name = "parent_station", optional = true)
  private int parentStation;

  @CsvField(optional = true)
  private Double direction;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public Point getLocation() {
    return location;
  }

  public void setLocation(Point location) {
    this.location = location;
  }

  public int getZoneId() {
    return zoneId;
  }

  public void setZoneId(int zoneId) {
    this.zoneId = zoneId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getLocationType() {
    return locationType;
  }

  public void setLocationType(int locationType) {
    this.locationType = locationType;
  }

  public int getParentStation() {
    return parentStation;
  }

  public void setParentStation(int parentStation) {
    this.parentStation = parentStation;
  }

  public Double getDirection() {
    return direction;
  }

  public void setDirection(Double direction) {
    this.direction = direction;
  }

  @Override
  public String toString() {
    return "Stop(" + this.id + ")";
  }
}
