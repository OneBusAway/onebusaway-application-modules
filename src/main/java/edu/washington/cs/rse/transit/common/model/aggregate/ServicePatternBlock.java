package edu.washington.cs.rse.transit.common.model.aggregate;

import edu.washington.cs.rse.transit.common.model.ServicePattern;

import com.vividsolutions.jts.geom.Point;

import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "transit_service_pattern_blocks")
public class ServicePatternBlock {

  private static final long serialVersionUID = 1L;

  @Id
  private ServicePatternBlockKey id;

  private String description;

  @ManyToMany(fetch = FetchType.EAGER)
  @IndexColumn(name = "bookmark_position", base = 1)
  private List<ServicePattern> servicePatterns;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY")
  private Point startLocation;

  @Type(type = "org.hibernatespatial.GeometryUserType")
  @Column(columnDefinition = "GEOMETRY")
  private Point endLocation;

  public ServicePatternBlockKey getId() {
    return id;
  }

  public void setId(ServicePatternBlockKey id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<ServicePattern> getServicePatterns() {
    return servicePatterns;
  }

  public void setServicePatterns(List<ServicePattern> servicePatterns) {
    this.servicePatterns = servicePatterns;
  }

  public Point getStartLocation() {
    return startLocation;
  }

  public void setStartLocation(Point startLocation) {
    this.startLocation = startLocation;
  }

  public Point getEndLocation() {
    return endLocation;
  }

  public void setEndLocation(Point endLocation) {
    this.endLocation = endLocation;
  }
}
