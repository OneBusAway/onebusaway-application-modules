/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;

public class TimeRangeBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private long from;

  private long to;

  public TimeRangeBean() {

  }

  public TimeRangeBean(long from, long to) {
    this.from = from;
    this.to = to;
  }

  /**
   * 
   * @return the from time, or zero if not set
   */
  public long getFrom() {
    return from;
  }

  public void setFrom(long from) {
    this.from = from;
  }

  /**
   * 
   * @return the to time, or zero if not set
   */
  public long getTo() {
    return to;
  }

  public void setTo(long to) {
    this.to = to;
  }
}
