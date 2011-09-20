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
package org.onebusaway.api.model.transit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class TripStopTimesV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String timeZone;

  private List<TripStopTimeV2Bean> stopTimes = new ArrayList<TripStopTimeV2Bean>();

  private String previousTripId;

  private String nextTripId;

  private FrequencyV2Bean frequency;

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public List<TripStopTimeV2Bean> getStopTimes() {
    return stopTimes;
  }

  public void setStopTimes(List<TripStopTimeV2Bean> stopTimes) {
    this.stopTimes = stopTimes;
  }

  public String getPreviousTripId() {
    return previousTripId;
  }

  public void setPreviousTripId(String previousTripId) {
    this.previousTripId = previousTripId;
  }

  public String getNextTripId() {
    return nextTripId;
  }

  public void setNextTripId(String nextTripId) {
    this.nextTripId = nextTripId;
  }

  public FrequencyV2Bean getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyV2Bean frequency) {
    this.frequency = frequency;
  }
}
