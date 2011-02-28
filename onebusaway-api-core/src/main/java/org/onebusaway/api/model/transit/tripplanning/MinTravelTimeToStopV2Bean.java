package org.onebusaway.api.model.transit.tripplanning;

import java.io.Serializable;

import org.onebusaway.csv_entities.schema.FlattenFieldMappingFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.geospatial.model.CoordinatePoint;

public class MinTravelTimeToStopV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String stopId;

  @CsvField(mapping = FlattenFieldMappingFactory.class)
  private CoordinatePoint location;

  private long travelTime;

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public CoordinatePoint getLocation() {
    return location;
  }

  public void setLocation(CoordinatePoint location) {
    this.location = location;
  }

  public long getTravelTime() {
    return travelTime;
  }

  public void setTravelTime(long travelTime) {
    this.travelTime = travelTime;
  }
}
