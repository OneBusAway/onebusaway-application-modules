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

import org.onebusaway.collections.FactoryMap;
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
    Map<FrequencyLabelKey, List<FrequencyEntry>> frequencyLabelsByKey = new FactoryMap<FrequencyLabelKey, List<FrequencyEntry>>(
        new ArrayList<FrequencyEntry>());

    Collection<Frequency> allFrequencies = _gtfsDao.getAllFrequencies();

    int frequencyIndex = 0;
    Map<AgencyAndId, Integer> exactTimesValueByTrip = new HashMap<AgencyAndId, Integer>();

    for (Frequency frequency : allFrequencies) {

      if (frequencyIndex % 100 == 0)
        _log.info("frequencies: " + (frequencyIndex++) + "/"
            + allFrequencies.size());
      frequencyIndex++;

      processRawFrequency(graph, frequency, frequenciesByTripId,
          frequencyLabelsByKey, exactTimesValueByTrip);
    }

    FrequencyComparator comparator = new FrequencyComparator();
    for (List<FrequencyEntry> list : frequenciesByTripId.values()) {
      Collections.sort(list, comparator);
    }

    applyFrequencyLabelsToTrips(graph, frequencyLabelsByKey);

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
      Map<AgencyAndId, FrequencyEntry> frequencyLabelsAlongBlockByTripId = new HashMap<AgencyAndId, FrequencyEntry>();

      for (TripEntryImpl trip : tripsInBlock) {
        List<FrequencyEntry> frequencies = frequenciesByTripId.get(trip.getId());
        if (frequencies != null) {
          frequenciesAlongBlockByTripId.put(trip.getId(), frequencies);
        }
        if (trip.getFrequencyLabel() != null) {
          frequencyLabelsAlongBlockByTripId.put(trip.getId(),
              trip.getFrequencyLabel());
        }
      }

      checkForInvalidFrequencyConfigurations(blockId, tripsInBlock,
          frequenciesAlongBlockByTripId, frequencyLabelsAlongBlockByTripId);

      if (!frequenciesAlongBlockByTripId.isEmpty()) {
        applyFrequenciesToBlockTrips(tripsInBlock,
            frequenciesAlongBlockByTripId);
      } else if (!frequencyLabelsAlongBlockByTripId.isEmpty()) {

      } else {
      }
    }
  }

  private void processRawFrequency(TransitGraphImpl graph, Frequency frequency,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesByTripId,
      Map<FrequencyLabelKey, List<FrequencyEntry>> frequencyLabelsByKey,
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
        frequency.getEndTime(), frequency.getHeadwaySecs());

    if (exactTimesValue == 0) {
      List<FrequencyEntry> frequencies = frequenciesByTripId.get(tripId);

      if (frequencies == null) {
        frequencies = new ArrayList<FrequencyEntry>();
        frequenciesByTripId.put(tripId, frequencies);
      }

      frequencies.add(entry);
    } else if (exactTimesValue == 2) {
      TripEntry trip = graph.getTripEntryForId(tripId);
      FrequencyLabelKey frequencyLabelKey = getFrequencyLabelKeyForTrip(trip);
      List<FrequencyEntry> frequencyLabels = frequencyLabelsByKey.get(frequencyLabelKey);
      frequencyLabels.add(entry);
    }
  }

  private void checkForInvalidFrequencyConfigurations(AgencyAndId blockId,
      List<TripEntryImpl> tripsInBlock,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesAlongBlockByTripId,
      Map<AgencyAndId, FrequencyEntry> frequencyLabelsAlongBlockByTripId) {

    if (!frequenciesAlongBlockByTripId.isEmpty()
        && !frequencyLabelsAlongBlockByTripId.isEmpty()) {
      throw new IllegalStateException(
          "A block of trips cannot have trips with both normal Frequency entries and label Frequency entries: blockId="
              + blockId);
    }

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

  private void applyFrequencyLabelsToTrips(TransitGraphImpl graph,
      Map<FrequencyLabelKey, List<FrequencyEntry>> frequencyLabelsByKey) {

    for (TripEntry trip : graph.getAllTrips()) {
      List<FrequencyEntry> applicableFrequencyLabels = getApplicableFrequencyLabelsForTrip(
          frequencyLabelsByKey, trip);
      if (applicableFrequencyLabels.isEmpty()) {
        continue;
      } else if (applicableFrequencyLabels.size() > 1) {
        throw new IllegalStateException(
            "multiple applicable frequency labels for tripId=" + trip.getId());
      }

      FrequencyEntry frequencyLabel = applicableFrequencyLabels.get(0);
      ((TripEntryImpl) trip).setFrequencyLabel(frequencyLabel);
    }
  }

  private List<FrequencyEntry> getApplicableFrequencyLabelsForTrip(
      Map<FrequencyLabelKey, List<FrequencyEntry>> frequencyLabelsByKey,
      TripEntry trip) {

    FrequencyLabelKey key = getFrequencyLabelKeyForTrip(trip);
    List<FrequencyEntry> labels = frequencyLabelsByKey.get(key);
    if (labels == null) {
      return Collections.emptyList();
    }
    List<FrequencyEntry> applicableLabels = new ArrayList<FrequencyEntry>();
    int startTime = trip.getStopTimes().get(0).getDepartureTime();
    for (FrequencyEntry label : labels) {
      if (label.getStartTime() <= startTime && startTime < label.getEndTime()) {
        applicableLabels.add(label);
      }
    }
    return applicableLabels;
  }

  private FrequencyLabelKey getFrequencyLabelKeyForTrip(TripEntry trip) {
    AgencyAndId routeId = trip.getRoute().getId();
    AgencyAndId serviceId = trip.getServiceId().getId();
    List<AgencyAndId> stopIds = MappingLibrary.map(trip.getStopTimes(),
        "stop.id");
    return new FrequencyLabelKey(routeId, serviceId, stopIds);
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
