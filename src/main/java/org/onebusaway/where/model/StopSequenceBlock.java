package org.onebusaway.where.model;

import com.vividsolutions.jts.geom.Point;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.onebusaway.common.model.IdentityBean;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "where_stop_sequence_blocks")
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class StopSequenceBlock extends IdentityBean<StopSequenceBlockKey> {

  private static final long serialVersionUID = 1L;

  @Id
  @AccessType("property")
  private StopSequenceBlockKey id;

  private String description;

  @ManyToMany(fetch = FetchType.EAGER)
  private List<StopSequence> stopSequences;

  private double startLat;

  private double startLon;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY")
  private Point startLocation;

  private double endLat;

  private double endLon;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY")
  private Point endLocation;

  public StopSequenceBlockKey getId() {
    return id;
  }

  public void setId(StopSequenceBlockKey id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<StopSequence> getStopSequences() {
    return stopSequences;
  }

  public void setStopSequences(List<StopSequence> stopSequences) {
    this.stopSequences = stopSequences;
  }

  public Point getStartLocation() {
    return startLocation;
  }

  public double getStartLat() {
    return startLat;
  }

  public void setStartLat(double startLat) {
    this.startLat = startLat;
  }

  public double getStartLon() {
    return startLon;
  }

  public void setStartLon(double startLon) {
    this.startLon = startLon;
  }

  public void setStartLocation(Point startLocation) {
    this.startLocation = startLocation;
  }

  public double getEndLat() {
    return endLat;
  }

  public void setEndLat(double endLat) {
    this.endLat = endLat;
  }

  public double getEndLon() {
    return endLon;
  }

  public void setEndLon(double endLon) {
    this.endLon = endLon;
  }

  public Point getEndLocation() {
    return endLocation;
  }

  public void setEndLocation(Point endLocation) {
    this.endLocation = endLocation;
  }

  @Override
  public String toString() {
    return "StopSequenceBlock(id=" + this.id + " desc=" + this.description
        + ")";
  }
}
