/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.trips.TripBean;

public final class TripStopTimesBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<TripStopTimeBean> stopTimes = new ArrayList<TripStopTimeBean>();

  private TripBean previousTrip;

  private TripBean nextTrip;

  private String timeZone;

  private FrequencyBean frequency;

  public List<TripStopTimeBean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<TripStopTimeBean> stopTimes) {
    this.stopTimes = stopTimes;
  }

  public void addStopTime(TripStopTimeBean stopTime) {
    stopTimes.add(stopTime);
  }

  public TripBean getPreviousTrip() {
    return previousTrip;
  }

  public void setPreviousTrip(TripBean previousTrip) {
    this.previousTrip = previousTrip;
  }

  public TripBean getNextTrip() {
    return nextTrip;
  }

  public void setNextTrip(TripBean nextTrip) {
    this.nextTrip = nextTrip;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public FrequencyBean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyBean frequency) {
    this.frequency = frequency;
  }
}
