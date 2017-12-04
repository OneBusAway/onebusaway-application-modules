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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.Block;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.model.bundle.BlockRunIndex;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BlockRunTask implements Runnable {

  private FederatedTransitDataBundle _bundle;
  private GtfsRelationalDao _gtfsDao;
  private Logger _log = LoggerFactory.getLogger(BlockRunTask.class);
  
  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }
  
  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }
  
  @Override
  public void run() {
    try {
      _log.error("BlockRunTask with " + _gtfsDao.getAllBlocks().size() + " blocks");
      Map<String, List<BlockRunIndex>> map = new HashMap<String, List<BlockRunIndex>>();
      for (Block blockRun : _gtfsDao.getAllBlocks()) {
          addRunBlock(map, blockRun.getBlockRun(), blockRun.getBlockRoute(), blockRun.getBlockSequence(), blockRun.getBlockVariable());
      }
      
      ObjectSerializationLibrary.writeObject(_bundle.getBlockRunDataPath(), map);
    } catch (Exception ex) {
      _log.error("fatal exception building BlockRunIndex:", ex);
    }
    
  }

private void addRunBlock(Map<String, List<BlockRunIndex>> map, int blockRun, int blockRoute, int blockSequence,
      int blockVariable) {
  String key = hash(blockRoute, blockRun);
  List<BlockRunIndex> list;
  if (!map.containsKey(key)) {
    list = new ArrayList<BlockRunIndex>();
    map.put(key, list);
  } else {
    list = map.get(key);
  }
  
  
  BlockRunIndex.Builder index = BlockRunIndex.builder();
  index.setRunId(blockRun);
  index.setRouteKey(blockRoute);
  index.setBlockId(blockSequence);
  list.add(index.create());
    
  }

  
  private String hash(Integer routeKey, Integer runId) {
    if (routeKey == null || runId == null) return null;
    return routeKey + ":" + runId;
  }
}
