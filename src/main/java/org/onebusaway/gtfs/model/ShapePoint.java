package org.onebusaway.gtfs.model;

import com.vividsolutions.jts.geom.Point;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;
import org.onebusaway.gtfs.serialization.LocationFieldMappingFactory;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "gtfs_shape_points")
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
@CsvFields(filename = "shapes.txt")
public class ShapePoint extends IdentityBean<ShapePointKey> {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @AccessType("property")
  private ShapePointKey id;

  @Index(name="distTraveled")
  @CsvField(name = "shape_dist_traveled", optional = true)
  private double distTraveled;

  @CsvField(name = "shape_pt_lat")
  private double lat;

  @CsvField(name = "shape_pt_lon")
  private double lon;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY")
  @Index(name = "location")
  @CsvField(mapping = LocationFieldMappingFactory.class)
  private Point location;

  public ShapePointKey getId() {
    return id;
  }

  public void setId(ShapePointKey id) {
    this.id = id;
  }

  public double getDistTraveled() {
    return distTraveled;
  }

  public void setDistTraveled(double distTraveled) {
    this.distTraveled = distTraveled;
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
}
