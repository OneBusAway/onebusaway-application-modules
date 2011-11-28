/**
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
package org.onebusaway.transit_data_federation.services.blocks;

import java.text.DateFormat;
import java.util.Date;

import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;

/**
 * Captures 'instance' information about a particular block, trip, or stop time.
 * By default, this includes the service date that a particular block, trip, or
 * stop time is operating on. It can also include {@link FrequencyEntry}
 * information about which headway-based interval a block, trip, or stop-time is
 * operating in.
 * 
 * @author bdferris
 * 
 */
public class InstanceState {

  private static final DateFormat _serviceDateFormat = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT);

  private final long _serviceDate;

  private final FrequencyEntry _frequency;

  public InstanceState(long serviceDate) {
    this(serviceDate, null);
  }

  public InstanceState(long serviceDate, FrequencyEntry frequency) {
    _serviceDate = serviceDate;
    _frequency = frequency;
  }

  public long getServiceDate() {
    return _serviceDate;
  }

  public FrequencyEntry getFrequency() {
    return _frequency;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_frequency == null) ? 0 : _frequency.hashCode());
    result = prime * result + (int) (_serviceDate ^ (_serviceDate >>> 32));
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
    InstanceState other = (InstanceState) obj;
    if (_frequency == null) {
      if (other._frequency != null)
        return false;
    } else if (!_frequency.equals(other._frequency))
      return false;
    if (_serviceDate != other._serviceDate)
      return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("serviceDate=");
    b.append(_serviceDateFormat.format(new Date(_serviceDate)));
    if (_frequency != null) {
      b.append(" ");
      b.append(_frequency);
    }
    return b.toString();
  }
}
