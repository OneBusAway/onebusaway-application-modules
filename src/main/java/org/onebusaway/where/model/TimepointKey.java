/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class TimepointKey implements Serializable {

  private static final long serialVersionUID = 1L;

  private String tripId;

  private String timepointId;

  private int timepointSequence;

  public TimepointKey() {

  }

  public TimepointKey(String tripId, String timepointId, int timepointSequence) {
    this.tripId = tripId;
    this.timepointId = timepointId;
    this.timepointSequence = timepointSequence;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getTimepointId() {
    return timepointId;
  }

  public void setTimepointId(String timepointId) {
    this.timepointId = timepointId;
  }

  public int getTimepointSequence() {
    return timepointSequence;
  }

  public void setTimepointSequence(int timepointSequence) {
    this.timepointSequence = timepointSequence;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof TimepointKey))
      return false;

    TimepointKey key = (TimepointKey) obj;
    return this.tripId.equals(key.tripId)
        && this.timepointId.equals(key.timepointId)
        && this.timepointSequence == key.timepointSequence;
  }

  @Override
  public int hashCode() {
    return 5 * this.tripId.hashCode() + 7 * this.timepointId.hashCode() + 11
        * this.timepointSequence;
  }

  @Override
  public String toString() {
    return this.tripId + " " + this.timepointId + " " + this.timepointSequence;
  }
}
