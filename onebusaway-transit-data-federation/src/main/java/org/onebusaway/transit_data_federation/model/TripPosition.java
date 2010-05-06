package org.onebusaway.transit_data_federation.model;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data_federation.model.predictions.ScheduleDeviation;

public class TripPosition {
  
  private ScheduleDeviation scheduleDeviation;
  
  private CoordinatePoint position;

  public ScheduleDeviation getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(ScheduleDeviation scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public CoordinatePoint getPosition() {
    return position;
  }

  public void setPosition(CoordinatePoint position) {
    this.position = position;
  }
}
