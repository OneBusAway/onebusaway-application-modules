/**
 * 
 */
package org.onebusaway.metrokc2gtdf.model;

import org.onebusaway.csv.CsvFields;

@CsvFields(filename = "stop_times.csv")
public class MetroKCStopTime {

  private int tripId;

  private int patternTimepointPosition;

  private double passingTime;

  private int timepoint;

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
}