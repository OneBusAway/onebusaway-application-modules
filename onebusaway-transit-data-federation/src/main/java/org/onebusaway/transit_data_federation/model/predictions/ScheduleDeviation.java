package org.onebusaway.transit_data_federation.model.predictions;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Schedule deviation captures information about the "on-time status" of a
 * transit vehicle. The {@link #getScheduleDeviation()} is a measurement, in
 * seconds, of the on-time status. A positive value indicates the transit
 * vehicle is running late, while a negative value indicates the vehicle is
 * running early. When real-time data isn't available, {@link #isPredicted()}
 * will be false and the schedule deviation will be zero.
 * 
 * @author bdferris
 * 
 */
public class ScheduleDeviation {

  private boolean predicted;

  private int scheduleDeviation;

  private AgencyAndId vehicleId;

  /**
   * If real-time data is not avaialble, schedule deviation will be zero
   * 
   * @return true if the schedule deviation data is from real-time data
   */
  public boolean isPredicted() {
    return predicted;
  }

  public void setPredicted(boolean predicted) {
    this.predicted = predicted;
  }

  /**
   * If {@link #isPredicted()} is false, indicating no real-time data is
   * available, schedule deviation will be zero.
   * 
   * @return schedule deviation, in seconds, (+deviation is late, -deviation is
   *         early)
   */
  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  /**
   * 
   * @param scheduleDeviation schedule deviation, in seconds, (+deviation is
   *          late, -deviation is early)
   */
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
