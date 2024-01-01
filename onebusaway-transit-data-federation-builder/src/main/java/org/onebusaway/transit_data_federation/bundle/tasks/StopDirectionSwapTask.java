/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.WrongWayConcurrency;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data.model.StopDirectionSwap;
import org.onebusaway.transit_data_federation.impl.StopDirectionSwapKey;

import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Process optional wrong way concurrencies.
 */
public class StopDirectionSwapTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(StopDirectionSwapTask.class);

  @Autowired
  private GtfsRelationalDao _gtfsDao;
  @Autowired
  private FederatedTransitDataBundle _bundle;


  @Override
  public void run() {
    if (_gtfsDao.getAllWrongWayConcurrencies() == null) {
      _log.debug("no wrong way concurrencies to load");
      return;
    }
    Map<StopDirectionSwapKey, StopDirectionSwap> cache = new HashMap<>();
    for (WrongWayConcurrency wrongWay : _gtfsDao.getAllWrongWayConcurrencies()) {
      StopDirectionSwapKey key
              = new StopDirectionSwapKey(AgencyAndId.convertFromString(wrongWay.getRouteId()),
                                                              wrongWay.getDirectionId(), wrongWay.getFromStopId());
      StopDirectionSwap swap = new StopDirectionSwap();
      swap.setRouteId(AgencyAndId.convertFromString(wrongWay.getRouteId()));
      swap.setDirectionId(wrongWay.getDirectionId());
      swap.setFromStop(wrongWay.getFromStopId());
      swap.setToStop(wrongWay.getToStopId());
      cache.put(key, swap);
    }
    writeObject(cache);

  }

  private void writeObject(Map<StopDirectionSwapKey, StopDirectionSwap> cache) {
    try {
      ObjectSerializationLibrary.writeObject(_bundle.getStopSwapPath(), cache);
    } catch (IOException ioe) {
      _log.error("fatal exception building StopDirectionSwaps:", ioe, ioe);
    }
  }
}
