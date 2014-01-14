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
package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternData;

public class TPQueryData {

  /**
   * We keep one instance of {@link TransferPatternData} per transfer-pattern-based
   * trip planning request so that we can unify the transfer patterns across the
   * request
   */
  private final TransferPatternData transferPatternData = new TransferPatternData();

  private final List<StopEntry> sourceStops;

  private final List<StopEntry> destStops;

  public TPQueryData(Set<StopEntry> sourceStops, Set<StopEntry> destStops) {
    this.sourceStops = new ArrayList<StopEntry>(sourceStops);
    this.destStops = new ArrayList<StopEntry>(destStops);
    Collections.sort(this.sourceStops);
    Collections.sort(this.destStops);
  }
  
  public TransferPatternData getTransferPatternData() {
    return transferPatternData;
  }

  public List<StopEntry> getSourceStops() {
    return sourceStops;
  }

  public List<StopEntry> getDestStops() {
    return destStops;
  }
}
