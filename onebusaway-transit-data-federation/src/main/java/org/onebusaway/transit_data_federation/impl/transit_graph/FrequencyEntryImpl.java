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
package org.onebusaway.transit_data_federation.impl.transit_graph;

import java.io.Serializable;

import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

public class FrequencyEntryImpl implements FrequencyEntry, Serializable {

  private static final long serialVersionUID = 2L;

  private final int startTime;
  private final int endTime;
  private final int headwaySecs;
  private final int exactTimes;

  public FrequencyEntryImpl(int startTime, int endTime, int headwaySecs, int exactTimes) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.headwaySecs = headwaySecs;
    this.exactTimes = exactTimes;
  }

  @Override
  public int getStartTime() {
    return startTime;
  }

  @Override
  public int getEndTime() {
    return endTime;
  }

  @Override
  public int getHeadwaySecs() {
    return headwaySecs;
  }
  
  @Override
  public int getExactTimes() {
    return exactTimes;
  }

  @Override
  public String toString() {
    return "startTime=" + startTime + " endTime=" + endTime + " headwaySecs="
        + headwaySecs + " exactTimes=" + exactTimes;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + endTime;
    result = prime * result + headwaySecs;
    result = prime * result + startTime;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FrequencyEntryImpl other = (FrequencyEntryImpl) obj;
    if (endTime != other.endTime)
      return false;
    if (headwaySecs != other.headwaySecs)
      return false;
    if (startTime != other.startTime)
      return false;
    return true;
  }
}
