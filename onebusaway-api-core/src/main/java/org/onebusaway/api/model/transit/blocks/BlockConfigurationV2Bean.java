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
package org.onebusaway.api.model.transit.blocks;

import java.io.Serializable;
import java.util.List;

public class BlockConfigurationV2Bean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<String> activeServiceIds;

  private List<String> inactiveServiceIds;

  private List<BlockTripV2Bean> trips;

  public List<String> getActiveServiceIds() {
    return activeServiceIds;
  }

  public void setActiveServiceIds(List<String> activeServiceIds) {
    this.activeServiceIds = activeServiceIds;
  }

  public List<String> getInactiveServiceIds() {
    return inactiveServiceIds;
  }

  public void setInactiveServiceIds(List<String> inactiveServiceIds) {
    this.inactiveServiceIds = inactiveServiceIds;
  }

  public List<BlockTripV2Bean> getTrips() {
    return trips;
  }

  public void setTrips(List<BlockTripV2Bean> trips) {
    this.trips = trips;
  }
}
