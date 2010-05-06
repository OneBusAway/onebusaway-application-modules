package org.onebusaway.where.model;

import org.onebusaway.common.model.IdentityBean;
import org.onebusaway.csv.CsvField;
import org.onebusaway.csv.CsvFields;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "where_timepoints")
@AccessType("field")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@CsvFields(filename = "timepoints.txt")
public class Timepoint extends IdentityBean<TimepointKey> {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @AccessType("property")
  private TimepointKey id;

  private String gtfsTripId;

  @CsvField(optional = true)
  private double shapeDistanceTraveled;

  public TimepointKey getId() {
    return id;
  }

  public void setId(TimepointKey id) {
    this.id = id;
  }

  public String getGtfsTripId() {
    return gtfsTripId;
  }

  public void setGtfsTripId(String gtfsTripId) {
    this.gtfsTripId = gtfsTripId;
  }

  public double getShapeDistanceTraveled() {
    return shapeDistanceTraveled;
  }

  public void setShapeDistanceTraveled(double shapeDistanceTraveled) {
    this.shapeDistanceTraveled = shapeDistanceTraveled;
  }
}
