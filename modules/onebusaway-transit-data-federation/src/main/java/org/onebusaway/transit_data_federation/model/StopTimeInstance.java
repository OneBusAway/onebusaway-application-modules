package org.onebusaway.transit_data_federation.model;

import org.onebusaway.gtfs.model.StopTime;

import java.util.Date;

public class StopTimeInstance {

  private StopTime _stopTime;

  private Date _date;

  private boolean _hasPredictedArrivalOffset = false;

  private boolean _hasPredictedDepartureOffset = false;

  private long _predictedArrivalOffset = 0;

  private long _predictedDepartureOffset = 0;

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

  public boolean hasPredictedArrivalOffset() {
    return _hasPredictedArrivalOffset;
  }

  public void setPredictedArrivalOffset(long offset) {
    _hasPredictedArrivalOffset = true;
    _predictedArrivalOffset = offset;
  }

  public long getPredictedArrivalOffset() {
    return _predictedArrivalOffset;
  }

  public boolean hasPredictedDepartureOffset() {
    return _hasPredictedDepartureOffset;
  }

  public void setPredictedDepartureOffset(long offset) {
    _hasPredictedDepartureOffset = true;
    _predictedDepartureOffset = offset;
  }

  public long getPredictedDepartureOffset() {
    return _predictedDepartureOffset;
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
