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

public final class FrequencyV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long startTime;

  private long endTime;

  private int headway;

  private int exactTimes;

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public int getHeadway() {
    return headway;
  }

  public void setHeadway(int headwaySecs) {
    this.headway = headwaySecs;
  }

  public int getExactTimes() {
    return exactTimes;
  }

  public void setExactTimes(int exact_times) {
    this.exactTimes = exact_times;
  }
}
