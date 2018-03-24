/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2015 University of South Florida
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.realtime.api;

import java.io.Serializable;

import org.onebusaway.gtfs.model.AgencyAndId;

public class TimepointPredictionRecord implements Serializable {

  private static final long serialVersionUID = 2L;

  /**
   * 
   */
  private AgencyAndId timepointId;
  
  private AgencyAndId tripId;
  
  private int stopSequence = -1;

  // times in ms
  
  private long timepointScheduledTime;

  private long timepointPredictedArrivalTime = -1;

  private long timepointPredictedDepartureTime = -1;

  public TimepointPredictionRecord() {

  }

  public TimepointPredictionRecord(TimepointPredictionRecord r) {
    this.timepointId = r.timepointId;
    this.tripId = r.tripId;
    this.stopSequence = r.stopSequence;
    this.timepointPredictedArrivalTime = r.timepointPredictedArrivalTime;
    this.timepointPredictedDepartureTime = r.timepointPredictedDepartureTime;
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

  public AgencyAndId getTripId() {
	  return tripId;
  }

  public void setTripId(AgencyAndId tripId) {
	  this.tripId = tripId;
  }

  public int getStopSequence() {
	  return stopSequence;
  }

  public void setStopSequence(int stopSequence) {
	  this.stopSequence = stopSequence;
  }

  public long getTimepointPredictedArrivalTime() {
	  return timepointPredictedArrivalTime;
  }

  public void setTimepointPredictedArrivalTime(long timepointPredictedArrivalTime) {
	  this.timepointPredictedArrivalTime = timepointPredictedArrivalTime;
  }

  public long getTimepointPredictedDepartureTime() {
	  return timepointPredictedDepartureTime;
  }

  public void setTimepointPredictedDepartureTime(
		  long timepointPredictedDepartureTime) {
	  this.timepointPredictedDepartureTime = timepointPredictedDepartureTime;
  }
}
