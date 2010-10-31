package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.impl.transit_graph.BlockEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.FrequencyEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TransitGraphImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
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
    Map<AgencyAndId, List<FrequencyEntry>> frequenciesByTrip = getFrequenciesByTrip(graph);
    processBlockTrips(graph, tripsByBlockId, frequenciesByTrip);
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

        AgencyAndId blockId = trip.getId();

        if (trip.getBlockId() != null) {
          blockId = new AgencyAndId(trip.getId().getAgencyId(),
              trip.getBlockId());
        }

        tripsByBlockId.get(blockId).add(tripEntry);
      }
    }

    return tripsByBlockId;
  }

  private Map<AgencyAndId, List<FrequencyEntry>> getFrequenciesByTrip(
      TransitGraphImpl graph) {

    Map<AgencyAndId, List<FrequencyEntry>> frequenciesByTripId = new HashMap<AgencyAndId, List<FrequencyEntry>>();

    Collection<Frequency> allFrequencies = _gtfsDao.getAllFrequencies();

    int frequencyIndex = 0;

    for (Frequency frequency : allFrequencies) {

      if (frequencyIndex % 100 == 0)
        _log.info("frequencies: " + (frequencyIndex++) + "/"
            + allFrequencies.size());
      frequencyIndex++;

      AgencyAndId tripId = frequency.getTrip().getId();

      FrequencyEntryImpl entry = new FrequencyEntryImpl(
          frequency.getStartTime(), frequency.getEndTime(),
          frequency.getHeadwaySecs());

      List<FrequencyEntry> frequencies = frequenciesByTripId.get(tripId);

      if (frequencies == null) {
        frequencies = new ArrayList<FrequencyEntry>();
        frequenciesByTripId.put(tripId, frequencies);
      }

      frequencies.add(entry);
    }

    FrequencyComparator comparator = new FrequencyComparator();
    for (List<FrequencyEntry> list : frequenciesByTripId.values())
      Collections.sort(list, comparator);

    return frequenciesByTripId;
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
      Map<AgencyAndId, List<TripEntryImpl>> tripsByBlockId,
      Map<AgencyAndId, List<FrequencyEntry>> frequenciesByTrip) {

    int blockIndex = 0;

    for (Map.Entry<AgencyAndId, List<TripEntryImpl>> entry : tripsByBlockId.entrySet()) {

      if (blockIndex % 10 == 0)
        _log.info("block: " + blockIndex + "/" + tripsByBlockId.size());
      blockIndex++;

      AgencyAndId blockId = entry.getKey();
      List<TripEntryImpl> tripsInBlock = entry.getValue();

      if (tripsInBlock.isEmpty()) {
        _log.warn("no trips for block=" + blockId);
        continue;
      }

      Map<AgencyAndId, List<FrequencyEntry>> frequenciesAlongBlock = new HashMap<AgencyAndId, List<FrequencyEntry>>();
      for (TripEntryImpl trip : tripsInBlock) {
        List<FrequencyEntry> frequencies = frequenciesByTrip.get(trip.getId());
        if (frequencies != null)
          frequenciesAlongBlock.put(trip.getId(), frequencies);
      }

      if (frequenciesAlongBlock.size() > 0
          && frequenciesAlongBlock.size() < tripsInBlock.size()) {
        throw new IllegalStateException(
            "can't have mixture of trips with and without frequencies: blockId="
                + blockId);
      }

      BlockEntryImpl blockEntry = new BlockEntryImpl();
      blockEntry.setId(blockId);

      if (frequenciesAlongBlock.isEmpty()) {
        _blockConfigurationEntriesFactory.processBlockConfigurations(
            blockEntry, tripsInBlock);
      } else {
        _blockConfigurationEntriesFactory.processFrequencyBlockConfigurations(
            blockEntry, tripsInBlock, frequenciesAlongBlock);
      }

      graph.putBlockEntry(blockEntry);

      // Wire up the trip to block link
      for (TripEntryImpl trip : tripsInBlock)
        trip.setBlock(blockEntry);
    }
  }

}
