package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.FlattenFieldMappingFactory;
import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;
import org.onebusaway.gtfs.model.IdentityBean;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This class is officially deprecated, but support is kept around for the
 * legacy code base
 * 
 * @author bdferris
 */
@Entity
@Table(name = "where_timepoints")
@AccessType("field")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@CsvFields(filename = "timepoints.txt")
@Deprecated()
public class Timepoint extends IdentityBean<TimepointKey> {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  @AccessType("property")
  @CsvField(mapping=FlattenFieldMappingFactory.class)
  private TimepointKey id;

  private String trackerTripId;

  @CsvField(optional = true)
  private double shapeDistanceTraveled;

  public TimepointKey getId() {
    return id;
  }

  public void setId(TimepointKey id) {
    this.id = id;
  }

  public String getTrackerTripId() {
    return trackerTripId;
  }

  public void setTrackerTripId(String trackerTripId) {
    this.trackerTripId = trackerTripId;
  }

  public double getShapeDistanceTraveled() {
    return shapeDistanceTraveled;
  }

  public void setShapeDistanceTraveled(double shapeDistanceTraveled) {
    this.shapeDistanceTraveled = shapeDistanceTraveled;
  }
}
