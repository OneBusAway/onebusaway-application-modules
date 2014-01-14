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
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TransferPatternData {

  private final Map<Pair<StopEntry>, TransferNode> transfers = new HashMap<Pair<StopEntry>, TransferNode>();

  private final Map<StopEntry, HubNode> hubs = new HashMap<StopEntry, HubNode>();
  
  private final Map<StopEntry, TransferParent> hubNodes = new HashMap<StopEntry, TransferParent>();

  public Collection<TransferNode> getTransfers() {
    return transfers.values();
  }

  public Collection<HubNode> getHubs() {
    return hubs.values();
  }

  public TransferNode extendTree(StopEntry fromStop, StopEntry toStop,
      boolean exitAllowed) {
    Pair<StopEntry> stops = Tuples.pair(fromStop, toStop);
    TransferNode tree = transfers.get(stops);
    if (tree == null) {
      tree = new TransferNode(this, stops);
      transfers.put(stops, tree);
    }
    if (exitAllowed)
      tree.setExitAllowed(exitAllowed);
    return tree;
  }

  public void extendHub(StopEntry hubStop, Iterable<StopEntry> stopsTo) {
    HubNode hubNode = hubs.get(hubStop);
    if (hubNode == null) {
      hubNode = new HubNode(hubStop, stopsTo);
      hubs.put(hubStop, hubNode);
    }
  }

  public void clearMinRemainingWeights() {
    for (TransferNode node : transfers.values())
      node.setMinRemainingWeight(-1);
  }

  public TransferParent getNodesForHubStop(StopEntry hubStop) {
    return hubNodes.get(hubStop);
  }

  public void setNodesForHubStop(StopEntry hubStop, TransferParent nodes) {
    hubNodes.put(hubStop,nodes);
  }
}
