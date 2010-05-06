package org.onebusaway.transit_data_federation.model.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;

public class ScheduleDeviation {

  private boolean predicted;

  private int scheduleDeviation;

  private AgencyAndId vehicleId;

  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }
}
