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
package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class HubNode {

  private final StopEntry hubStop;

  private final Iterable<StopEntry> stopsTo;

  public HubNode(StopEntry hubStop, Iterable<StopEntry> stopsTo) {
    this.hubStop = hubStop;
    this.stopsTo = stopsTo;
  }

  public StopEntry getHubStop() {
    return hubStop;
  }

  public Iterable<StopEntry> getStopsTo() {
    return stopsTo;
  }

  @Override
  public String toString() {
    return "Hub(" + hubStop.getId() + ")";
  }
}
