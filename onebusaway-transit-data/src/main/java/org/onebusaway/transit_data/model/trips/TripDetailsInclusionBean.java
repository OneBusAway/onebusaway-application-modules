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
package org.onebusaway.transit_data.model.trips;

import java.io.Serializable;

public final class TripDetailsInclusionBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean includeTripBean = true;

  private boolean includeTripSchedule = true;

  private boolean includeTripStatus = true;

  public TripDetailsInclusionBean() {

  }

  public TripDetailsInclusionBean(boolean includeTripBean,
      boolean includeTripSchedule, boolean includeTripStatus) {
    this.includeTripBean = includeTripBean;
    this.includeTripSchedule = includeTripSchedule;
    this.includeTripStatus = includeTripStatus;
  }

  public boolean isIncludeTripBean() {
    return includeTripBean;
  }

  public void setIncludeTripBean(boolean includeTripBean) {
    this.includeTripBean = includeTripBean;
  }

  public boolean isIncludeTripSchedule() {
    return includeTripSchedule;
  }

  public void setIncludeTripSchedule(boolean includeTripSchedule) {
    this.includeTripSchedule = includeTripSchedule;
  }

  public boolean isIncludeTripStatus() {
    return includeTripStatus;
  }

  public void setIncludeTripStatus(boolean includeTripStatus) {
    this.includeTripStatus = includeTripStatus;
  }
}
