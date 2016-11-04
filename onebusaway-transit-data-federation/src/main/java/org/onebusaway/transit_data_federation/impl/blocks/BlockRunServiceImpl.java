/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.impl.blocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.model.bundle.BlockRunIndex;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.blocks.BlockRunService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockRunServiceImpl implements BlockRunService {

  private static Logger _log = LoggerFactory.getLogger(BlockRunServiceImpl.class);
  
  private FederatedTransitDataBundle _bundle;
  private Map<String, List<BlockRunIndex>> _map = null;
  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }
  
  @PostConstruct
  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void setup() throws IOException, ClassNotFoundException {
    _log.info("bundle path=" + _bundle.getPath());
    File path = _bundle.getBlockRunDataPath();
    if (path.exists()) {
      _log.info("loading BlockRunIndex...");
      _map = ObjectSerializationLibrary.readObject(path);
      _log.info("loading BlockRunIndex...done");
    } else {
      // this index is optional, do not fail if not found
      _log.info("failed BlockRunIndex load, path not found of " + path);
      _map = new HashMap<String, List<BlockRunIndex>>();
    }
  }
  
  public List<Integer> getBlockIds(Integer routeKey, Integer runId) {
    List<BlockRunIndex> index = _map.get(hash(routeKey, runId));
    if (index == null) {
      _log.debug("miss for hash=" + hash(routeKey, runId));
      return null;
    }
    List<Integer> blockIds = new ArrayList<Integer>();
    for (BlockRunIndex blockRunIndex : index) {
      blockIds.add(blockRunIndex.getBlockId());
    }
    return blockIds;
  }


  private String hash(Integer routeKey, Integer runId) {
    if (routeKey == null || runId == null) return null;
    return routeKey + ":" + runId;
  }

  // for testing we allow access to the map
  public void addRunBlock(int runId, int routeKey, int blockId) {
    String key = hash(routeKey, runId);
    List<BlockRunIndex> list;
    if (!_map.containsKey(key)) {
      list = new ArrayList<BlockRunIndex>();
      _map.put(key, list);
    } else {
      list = _map.get(key);
    }
    
    
    BlockRunIndex.Builder index = BlockRunIndex.builder();
    index.setRunId(runId);
    index.setRouteKey(routeKey);
    index.setBlockId(blockId);
    list.add(index.create());
  }
}
