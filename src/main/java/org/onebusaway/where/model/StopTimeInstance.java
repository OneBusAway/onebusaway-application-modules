package org.onebusaway.where.model;

import org.onebusaway.gtfs.model.StopTime;

import java.util.Date;

public class StopTimeInstance {

  private StopTime _stopTime;

  private Date _date;

  private boolean _hasPredictedOffset = false;

  private long _predictedOffset = 0;

  public StopTimeInstance(StopTime stopTime, Date date) {
    _stopTime = stopTime;
    _date = date;
  }

  public StopTime getStopTime() {
    return _stopTime;
  }

  public Date getDate() {
    return _date;
  }

  public Date getArrivalTime() {
    return new Date(_date.getTime() + _stopTime.getArrivalTime() * 1000);
  }

  public Date getDepartureTime() {
    return new Date(_date.getTime() + _stopTime.getDepartureTime() * 1000);
  }

  public boolean hasPrediction() {
    return _hasPredictedOffset;
  }

  public void setPredictedOffset(long offset) {
    _hasPredictedOffset = true;
    _predictedOffset = offset;
  }

  public long getPredictionOffset() {
    return _predictedOffset;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopTimeInstance))
      return false;
    StopTimeInstance sti = (StopTimeInstance) obj;
    return _stopTime.equals(sti._stopTime) && _date.equals(sti._date);
  }

  @Override
  public int hashCode() {
    return _stopTime.hashCode() + _date.hashCode();
  }
}
