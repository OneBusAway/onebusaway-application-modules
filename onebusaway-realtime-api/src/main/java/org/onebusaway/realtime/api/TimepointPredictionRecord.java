package org.onebusaway.realtime.api;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public class TimepointPredictionRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  private AgencyAndId timepointId;

  private long timepointScheduledTime;

  private long timepointPredictedTime;

  public TimepointPredictionRecord() {

  }

  public TimepointPredictionRecord(TimepointPredictionRecord r) {
    this.timepointId = r.timepointId;
    this.timepointPredictedTime = r.timepointPredictedTime;
    this.timepointScheduledTime = r.timepointScheduledTime;
  }

  public AgencyAndId getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(AgencyAndId timepointId) {
    this.timepointId = timepointId;
  }

  public long getTimepointScheduledTime() {
    return timepointScheduledTime;
  }

  public void setTimepointScheduledTime(long timepointScheduledTime) {
    this.timepointScheduledTime = timepointScheduledTime;
  }

  public long getTimepointPredictedTime() {
    return timepointPredictedTime;
  }

  public void setTimepointPredictedTime(long timepointPredictedTime) {
    this.timepointPredictedTime = timepointPredictedTime;
  }
}
