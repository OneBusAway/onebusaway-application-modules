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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.blocks.FrequencyComparator;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockConfigurationEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.FrequencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.util.LoggingIntervalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FrequencyEntriesFactory {

  private Logger _log = LoggerFactory.getLogger(FrequencyEntriesFactory.class);

  private GtfsRelationalDao _gtfsDao;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  public void processFrequencies(TransitGraphImpl graph) {

    Map<AgencyAndId, List<FrequencyEntry>> frequenciesByTripId = new HashMap<AgencyAndId, List<FrequencyEntry>>();

    Collection<Frequency> allFrequencies = _gtfsDao.getAllFrequencies();

	int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(allFrequencies.size());

    int frequencyIndex = 0;
    Map<AgencyAndId, Integer> exactTimesValueByTrip = new HashMap<AgencyAndId, Integer>();

    for (Frequency frequency : allFrequencies) {

      if (frequencyIndex % logInterval == 0)
        _log.info("frequencies: " + (frequencyIndex++) + "/"
            + allFrequencies.size());
      frequencyIndex++;

      processRawFrequency(graph, frequency, frequenciesByTripId, exactTimesValueByTrip);
    }

    FrequencyComparator comparator = new FrequencyComparator();
    for (List<FrequencyEntry> list : frequenciesByTripId.values()) {
      Collections.sort(list, comparator);
    }

    int blockIndex = 0;

    Map<AgencyAndId, List<TripEntryImpl>> tripsByBlockId = MappingLibrary.mapToValueList(
        graph.getTrips(), "block.id");

    for (Map.Entry<AgencyAndId, List<TripEntryImpl>> entry : tripsByBlockId.entrySet()) {

      if (blockIndex % 10 == 0)
        _log.info("block: " + blockIndex + "/" + tripsByBlockId.size());
      blockIndex++;

      AgencyAndId blockId = entry.getKey();
      List<TripEntryImpl> tripsInBlock = entry.getValue();

      Map<AgencyAndId, List<FrequencyEntry>> frequenciesAlongBlockByTripId = new HashMap<AgencyAndId, List<FrequencyEntry>>();

      for (TripEntryImpl trip : tripsInBlock) {
        List<FrequencyEntry> frequencies = frequenciesByTripId.get(trip.getId());
        if (frequencies != null) {
          frequenciesAlongBlockByTripId.put(trip.getId(), frequencies);
        }
      }

      checkForInvalidFrequencyConfigurations(blockId, tripsInBlock,
          frequenciesAlongBlockByTripId);

      applyFrequenciesToBlockTrips(tripsInBlock,
            frequenciesAlongBlockByTripId);
    }
  }

  private void processRawFrequency(TransitGraphImpl graph, Frequency frequency,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesByTripId,
      Map<AgencyAndId, Integer> exactTimesValueByTrip) {
    AgencyAndId tripId = frequency.getTrip().getId();

    /**
     * The value of frequencies.txt exact_times is expected to be the same
     * across all entries for the same trip id.
     */
    Integer exactTimesValue = exactTimesValueByTrip.get(tripId);
    if (exactTimesValue == null) {
      exactTimesValue = frequency.getExactTimes();
      exactTimesValueByTrip.put(tripId, exactTimesValue);
    } else if (exactTimesValue != frequency.getExactTimes()) {
      throw new IllegalStateException(
          "The value of frequencies.txt exact_times differed for frequency entries with tripId="
              + tripId);
    }

    FrequencyEntryImpl entry = new FrequencyEntryImpl(frequency.getStartTime(),
        frequency.getEndTime(), frequency.getHeadwaySecs(), frequency.getExactTimes());

      List<FrequencyEntry> frequencies = frequenciesByTripId.get(tripId);

      if (frequencies == null) {
        frequencies = new ArrayList<FrequencyEntry>();
        frequenciesByTripId.put(tripId, frequencies);
      }

      frequencies.add(entry);
  }

  private void checkForInvalidFrequencyConfigurations(AgencyAndId blockId,
      List<TripEntryImpl> tripsInBlock,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesAlongBlockByTripId) {

    if (!frequenciesAlongBlockByTripId.isEmpty()
        && frequenciesAlongBlockByTripId.size() < tripsInBlock.size()) {
      throw new IllegalStateException(
          "can't have mixture of trips with and without frequencies: blockId="
              + blockId);
    }
  }

  private void applyFrequenciesToBlockTrips(List<TripEntryImpl> tripsInBlock,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesAlongBlockByTripId) {

    BlockEntryImpl blockEntry = tripsInBlock.get(0).getBlock();
    List<BlockConfigurationEntry> configurations = blockEntry.getConfigurations();
    for (int i = 0; i < configurations.size(); i++) {
      BlockConfigurationEntryImpl blockConfig = (BlockConfigurationEntryImpl) configurations.get(i);
      List<FrequencyEntry> frequencies = computeBlockFrequencies(blockEntry,
          blockConfig.getTrips(), frequenciesAlongBlockByTripId);
      blockConfig.setFrequencies(frequencies);
    }
  }
  

  private List<FrequencyEntry> computeBlockFrequencies(BlockEntryImpl block,
      List<BlockTripEntry> trips,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesAlongBlock) {

    List<FrequencyEntry> frequencies = null;

    for (BlockTripEntry blockTrip : trips) {
      TripEntry trip = blockTrip.getTrip();
      List<FrequencyEntry> potentialFrequencies = frequenciesAlongBlock.get(trip.getId());

      if (frequencies == null) {
        frequencies = potentialFrequencies;
      } else {
        if (!frequencies.equals(potentialFrequencies)) {
          throw new IllegalStateException(
              "frequency-based trips in same block don't have same frequencies: blockId="
                  + block.getId());
        }
      }
    }

    return frequencies;
  }
}
