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
package org.onebusaway.transit_data_federation.model;

import java.io.Serializable;

import org.onebusaway.util.SystemTime;

/**
 * Typically, when it comes to real-time information, you want to query the
 * system right now. However, to assist with debugging, we make it possible to
 * specify what time 'now' actually is. This makes it easier to replay real-time
 * information from the past and query the state of the system at that time.
 * 
 * Thus, we have two times:
 * 
 * currentTime - the definition of now
 * 
 * targetTime - the time you want to query a particular value
 * 
 * For example, if I want to know where a bus will be five minutes from now, my
 * currentTime really will be now and my targetTime will be +5 minutes.
 * 
 * By the same token, if I'm pretending it's 1am on Oct 12th and I want to know
 * where the bus will be in five minutes, currentTime will be Oct 12th 1am and
 * targetTime will be Oct 12th 1:05 am.
 * 
 * @author bdferris
 */
public final class TargetTime implements Serializable {

  private static final long serialVersionUID = 1L;

  private final long currentTime;

  private final long targetTime;

  public TargetTime() {
    this(SystemTime.currentTimeMillis());
  }

  public TargetTime(long targetTime) {
    this(targetTime, SystemTime.currentTimeMillis());
  }

  public TargetTime(long targetTime, long currentTime) {
    this.currentTime = currentTime;
    this.targetTime = targetTime;
  }

  public long getCurrentTime() {
    return currentTime;
  }

  public long getTargetTime() {
    return targetTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (currentTime ^ (currentTime >>> 32));
    result = prime * result + (int) (targetTime ^ (targetTime >>> 32));
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
    TargetTime other = (TargetTime) obj;
    if (currentTime != other.currentTime)
      return false;
    if (targetTime != other.targetTime)
      return false;
    return true;
  }
}
