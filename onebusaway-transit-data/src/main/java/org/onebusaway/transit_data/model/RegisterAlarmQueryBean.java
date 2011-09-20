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

@QueryBean
public final class RegisterAlarmQueryBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean onArrival = false;

  private int alarmTimeOffset;

  private String url;

  public boolean isOnArrival() {
    return onArrival;
  }

  public void setOnArrival(boolean onArrival) {
    this.onArrival = onArrival;
  }

  public int getAlarmTimeOffset() {
    return alarmTimeOffset;
  }

  public void setAlarmTimeOffset(int alarmTimeOffset) {
    this.alarmTimeOffset = alarmTimeOffset;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
