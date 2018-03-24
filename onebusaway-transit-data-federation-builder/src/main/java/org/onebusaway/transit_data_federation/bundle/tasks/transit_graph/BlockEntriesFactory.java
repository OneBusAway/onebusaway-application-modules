/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.util.LoggingIntervalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockEntriesFactory {

  private Logger _log = LoggerFactory.getLogger(BlockEntriesFactory.class);

  private GtfsRelationalDao _gtfsDao;

  private BlockConfigurationEntriesFactory _blockConfigurationEntriesFactory;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setBlockConfigurationEntriesFactory(
      BlockConfigurationEntriesFactory blockConfigurationEntriesFactory) {
    _blockConfigurationEntriesFactory = blockConfigurationEntriesFactory;
  }

  public void processBlocks(TransitGraphImpl graph) {
    Map<AgencyAndId, List<TripEntryImpl>> tripsByBlockId = getTripsByBlockId(graph);
    processBlockTrips(graph, tripsByBlockId);
  }

  private Map<AgencyAndId, List<TripEntryImpl>> getTripsByBlockId(
      TransitGraphImpl graph) {

    Collection<Route> routes = _gtfsDao.getAllRoutes();
    int routeIndex = 0;

    Map<AgencyAndId, List<TripEntryImpl>> tripsByBlockId = new FactoryMap<AgencyAndId, List<TripEntryImpl>>(
        new ArrayList<TripEntryImpl>());

    for (Route route : routes) {

      _log.info("routes: " + (routeIndex++) + "/" + routes.size());

      List<Trip> trips = _gtfsDao.getTripsForRoute(route);

      for (Trip trip : trips) {

        TripEntryImpl tripEntry = graph.getTripEntryForId(trip.getId());

        // If null, probably indicates no stop times, or some other reason to
        // prune the trip
        if (tripEntry == null)
          continue;

        /*
         * here we default the blockId to the tripId
         */
        AgencyAndId blockId = trip.getId();

        if (trip.getBlockId() != null) {
          /*
           * he have a block so set it
           */
          blockId = new AgencyAndId(trip.getId().getAgencyId(),
              trip.getBlockId());
        }

        tripsByBlockId.get(blockId).add(tripEntry);
      }
    }

    return tripsByBlockId;
  }

  /**
   * We loop over blocks of trips, removing any trip that has no stop times,
   * sorting the remaining trips into the proper order, setting the 'nextTrip'
   * property for trips in the block, and setting the 'nextStop' property for
   * stops in the block.
   * 
   * @return
   */
  private void processBlockTrips(TransitGraphImpl graph,
      Map<AgencyAndId, List<TripEntryImpl>> tripsByBlockId) {

    int blockIndex = 0; 
	int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(tripsByBlockId.keySet().size());

    for (Map.Entry<AgencyAndId, List<TripEntryImpl>> entry : tripsByBlockId.entrySet()) {

      if (blockIndex % logInterval == 0)
        _log.info("block: " + blockIndex + "/" + tripsByBlockId.size());
      blockIndex++;

      AgencyAndId blockId = entry.getKey();
      List<TripEntryImpl> tripsInBlock = entry.getValue();

      if (tripsInBlock.isEmpty()) {
        _log.warn("no trips for block=" + blockId);
        continue;
      }

      BlockEntryImpl blockEntry = new BlockEntryImpl();
      blockEntry.setId(blockId);

      _blockConfigurationEntriesFactory.processBlockConfigurations(blockEntry,
          tripsInBlock);

      graph.putBlockEntry(blockEntry);

      // Wire up the trip to block link
      for (TripEntryImpl trip : tripsInBlock)
        trip.setBlock(blockEntry);
    }
  }
}
