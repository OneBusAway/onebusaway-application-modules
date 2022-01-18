/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.gtfs_realtime.archiver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ArrivalTime {

  @JsonProperty("Actual") private String actual;
  @JsonProperty("Scheduled") private String scheduled;
  @JsonProperty("Estimated") private String estimated;

  public String getActual() {
    return actual;
  }
  public void setActual(String actual) {
    this.actual = actual;
  }
  public String getScheduled() {
    return scheduled;
  }
  public void setScheduled(String scheduled) {
    this.scheduled = scheduled;
  }
  public String getEstimated() {
    return estimated;
  }
  public void setEstimated(String estimated) {
    this.estimated = estimated;
  }
}
