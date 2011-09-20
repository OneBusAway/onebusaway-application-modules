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
package org.onebusaway.transit_data_federation.services.tripplanner;

import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.HubNode;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferPatternData;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferNode;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferParent;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public interface TransferPatternService {

  /**
   * 
   * @return true is transfer path functionality is enabled
   */
  public boolean isEnabled();

  public TransferParent getTransferPatternsForStops(
      TransferPatternData transferData, StopEntry stopFrom, List<StopEntry> stopsTo);

  public Collection<TransferNode> getReverseTransferPatternsForStops(
      TransferPatternData transferData, Iterable<StopEntry> stopsFrom, StopEntry stopTo);

  public Collection<TransferNode> expandNode(HubNode node);
}
