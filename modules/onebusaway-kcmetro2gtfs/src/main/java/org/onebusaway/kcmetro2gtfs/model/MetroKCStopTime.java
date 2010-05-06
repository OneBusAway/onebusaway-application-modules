/**
 * 
 */
package org.onebusaway.kcmetro2gtfs.model;

import org.onebusaway.gtfs.csv.schema.annotations.CsvFields;

@CsvFields(filename = "stop_times.csv")
public class MetroKCStopTime {

  private String changeDate;

  private int tripId;

  private int patternTimepointPosition;

  private double passingTime;

  private int timepoint;

  public String getChangeDate() {
    return changeDate;
  }

  public void setChangeDate(String changeDate) {
    this.changeDate = changeDate;
  }

  public int getTripId() {
    return tripId;
  }

  public void setTripId(int tripId) {
    this.tripId = tripId;
  }

  public int getPatternTimepointPosition() {
    return patternTimepointPosition;
  }

  public void setPatternTimepointPosition(int patternTimepointPosition) {
    this.patternTimepointPosition = patternTimepointPosition;
  }

  /**
   * @return passing time, in minutes since midnight
   */
  public double getPassingTime() {
    return passingTime;
  }

  public void setPassingTime(double passingTime) {
    this.passingTime = passingTime;
  }

  public int getTimepoint() {
    return timepoint;
  }

  public void setTimepoint(int timepoint) {
    this.timepoint = timepoint;
  }
  
  public ServicePatternKey getFullTripId() {
    return new ServicePatternKey(changeDate,tripId);
  }

  @Override
  public String toString() {
    return "StopTime(tripId=" + tripId + " timepoint=" + timepoint
        + " patternTimepointPosition=" + patternTimepointPosition
        + " passingTime=" + passingTime + ")";
  }

}