/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.bundle.tasks.stif;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.csv_entities.CSVListener;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.transit_data.model.oba.RunData;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.GeographyRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.ServiceCode;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.ObjectSerializationLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Load STIF data, including the mapping between destination sign codes and trip
 * ids, into the database
 * 
 * @author bdferris
 * 
 */
public class StifTask implements Runnable {

  private static final int MAX_BLOCK_ID_LENGTH = 64;

  private Logger _log = LoggerFactory.getLogger(StifTask.class);

  private GtfsMutableRelationalDao _gtfsMutableRelationalDao;
  
  private StifTripLoader _loader = null;

  private List<File> _stifPaths = new ArrayList<File>();
  
  private String _tripToDSCOverridePath;

  private Set<String> _notInServiceDscs = new HashSet<String>();

  private File _notInServiceDscPath;

  @Autowired 
  private FederatedTransitDataBundle _bundle;

  private boolean fallBackToStifBlocks = false;

  private MultiCSVLogger csvLogger = null;
  
  private HashMap<String, Set<AgencyAndId>> routeIdsByDsc = new HashMap<String, Set<AgencyAndId>>();

  @Autowired
  public void setLogger(MultiCSVLogger logger) {
    this.csvLogger = logger;
  }
  
  @Autowired
  public void setGtfsMutableRelationalDao(
      GtfsMutableRelationalDao gtfsMutableRelationalDao) {
    _gtfsMutableRelationalDao = gtfsMutableRelationalDao;
  }

  /**
   * The path of the directory containing STIF files to process
   */
  public void setStifPath(File path) {
    _stifPaths.add(path);
  }

  public void setStifPaths(List<File> paths) {
    _stifPaths.addAll(paths);
  }

  public void setNotInServiceDsc(String notInServiceDsc) {
    _notInServiceDscs.add(notInServiceDsc);
  }
  
  public void setTripToDSCOverridePath(String path) {
    _tripToDSCOverridePath = path;
  }

  public void setNotInServiceDscs(List<String> notInServiceDscs) {
    _notInServiceDscs.addAll(notInServiceDscs);
  }

  public void setNotInServiceDscPath(File notInServiceDscPath) {
    _notInServiceDscPath = notInServiceDscPath;
  }

  public void run() {

    if (_loader == null) {
      // we let the unit tests inject a custom loader
      _log.warn("creating loader with gtfs= " + _gtfsMutableRelationalDao + " and logger=" + csvLogger);
      _loader = new StifTripLoader();
      _loader.setGtfsDao(_gtfsMutableRelationalDao);
      _loader.setLogger(csvLogger);

      for (File path : _stifPaths) {
        loadStif(path, _loader);
      }

    }
    
    computeBlocksFromRuns(_loader);
    warnOnMissingTrips();

    if (fallBackToStifBlocks) {
      loadStifBlocks(_loader);
    }

    //store trip-run mapping in bundle
    Map<AgencyAndId, RunData> runsForTrip = _loader.getRunsForTrip();
    try {
      if (_bundle != null) // for unit tests
        ObjectSerializationLibrary.writeObject(_bundle.getTripRunDataPath(),
            runsForTrip);
    } catch (IOException e) {
          throw new IllegalStateException(e);
    }
    
    // non revenue moves
    serializeNonRevenueMoveData(_loader.getRawStifData(), _loader.getGeographyRecordsByBoxId());
    
    // non revenue stops
    serializeNonRevenueStopData(_loader.getNonRevenueStopDataByTripId());

    // dsc to trip map
    Map<String, List<AgencyAndId>> dscToTripMap = _loader.getTripMapping();
    
    // Read in trip to dsc overrides if they exist
    if (_tripToDSCOverridePath != null) {
      Map<AgencyAndId, String> tripToDSCOverrides;
      try {
        tripToDSCOverrides = loadTripToDSCOverrides(_tripToDSCOverridePath);
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }

      // Add tripToDSCOverrides to dscToTripMap
      for (Map.Entry<AgencyAndId, String> entry : tripToDSCOverrides.entrySet()) {
        
        if (_gtfsMutableRelationalDao.getTripForId(entry.getKey()) == null) {
          throw new IllegalStateException("Trip id " + entry.getKey() + " from trip ID to DSC overrides does not exist in bundle GTFS.");
        }
        
        List<AgencyAndId> agencyAndIds;
        
        // See if trips for this dsc are already in the map
        agencyAndIds = dscToTripMap.get(entry.getValue());

        // If not, care a new array of trip ids and add it to the map for this dsc
        if (agencyAndIds == null) {
          agencyAndIds = new ArrayList<AgencyAndId>();
          dscToTripMap.put(entry.getValue(), agencyAndIds);
        }

        // Add the trip id to our list of trip ids already associated with a dsc
        agencyAndIds.add(entry.getKey());
      }
    }
    
    Map<AgencyAndId, String> tripToDscMap = new HashMap<AgencyAndId, String>();
    
    // Populate tripToDscMap based on dscToTripMap
    for (Map.Entry<String, List<AgencyAndId>> entry : dscToTripMap.entrySet()) {
      String destinationSignCode = entry.getKey();
      List<AgencyAndId> tripIds = entry.getValue();
      for (AgencyAndId tripId : tripIds) {
       tripToDscMap.put(tripId, destinationSignCode);
      }
    }

    Set<String> inServiceDscs = new HashSet<String>();
    logDSCStatistics(dscToTripMap, tripToDscMap);

    int withoutMatch = _loader.getTripsWithoutMatchCount();
    int total = _loader.getTripsCount();

    _log.info("stif trips without match: " + withoutMatch + " / " + total);

    readNotInServiceDscs();
    serializeDSCData(dscToTripMap, tripToDscMap, inServiceDscs);
  }

  // package private for unit testing
  void logDSCStatistics(Map<String, List<AgencyAndId>> dscToTripMap,
      Map<AgencyAndId, String> tripToDscMap) {
    csvLogger.header("dsc_statistics.csv", "dsc,agency_id,number_of_trips_in_stif,number_of_distinct_route_ids_in_gtfs");
    for (Map.Entry<String, List<AgencyAndId>> entry : dscToTripMap.entrySet()) {
      String destinationSignCode = entry.getKey();
      List<AgencyAndId> tripIds = entry.getValue();

      Set<AgencyAndId> routeIds = routeIdsByDsc.get(destinationSignCode);
      HashSet<String> set = new HashSet<String>();
	  for (AgencyAndId aaid : tripIds){
		  if (aaid != null){
			  set.add(aaid.getAgencyId());
		  }
	  }
	  for (String agencyId : set){
		  csvLogger.log("dsc_statistics.csv", destinationSignCode,agencyId, tripIds.size(),(routeIds != null ? routeIds.size() : 0));
	  }
    }
  }

  private void serializeNonRevenueMoveData(Map<ServiceCode, List<StifTrip>> nonRevenueMovesByServiceCode, 
		  Map<AgencyAndId, GeographyRecord> nonRevenueMoveLocationsByBoxId) {
	  try {
	    if (_bundle != null) { // for unit tests
	      ObjectSerializationLibrary.writeObject(_bundle.getNonRevenueMovePath(), 
	          nonRevenueMovesByServiceCode);
	      ObjectSerializationLibrary.writeObject(_bundle.getNonRevenueMoveLocationsPath(), 
	          nonRevenueMoveLocationsByBoxId);
	    }
	  } catch (IOException e) {
		  throw new IllegalStateException("error serializing non-revenue move/STIF data", e);
	  }	  
  }
  
  private void serializeNonRevenueStopData(Map<AgencyAndId, List<NonRevenueStopData>> nonRevenueStopDataByTripId) {
    try {
      if (_bundle != null) // for unit tests
        ObjectSerializationLibrary.writeObject(_bundle.getNonRevenueStopsPath(), 
            nonRevenueStopDataByTripId);
    } catch (IOException e) {
      throw new IllegalStateException("error serializing non-revenue move/STIF data", e);
    }
  }
  
  private void serializeDSCData(Map<String, List<AgencyAndId>> dscToTripMap,
      Map<AgencyAndId, String> tripToDscMap, Set<String> inServiceDscs) {
    for (String notInServiceDsc : _notInServiceDscs) {
      if (inServiceDscs.contains(notInServiceDsc))
        _log.warn("overlap between in-service and not-in-service dscs: "
            + notInServiceDsc);

      // clear out trip mappings for out of service DSCs
      dscToTripMap.put(notInServiceDsc, new ArrayList<AgencyAndId>());
    }

    try {
      if (_bundle != null) { // for unit tests
          ObjectSerializationLibrary.writeObject(_bundle.getNotInServiceDSCs(), 
              _notInServiceDscs);

          ObjectSerializationLibrary.writeObject(_bundle.getTripsForDSCIndex(), 
              tripToDscMap);

          ObjectSerializationLibrary.writeObject(_bundle.getDSCForTripIndex(),
              dscToTripMap);
      }
    } catch (IOException e) {
      throw new IllegalStateException("error serializing DSC/STIF data", e);
    }
  }

  private void loadStifBlocks(StifTripLoader loader) {

    Map<Trip, RawRunData> rawData = loader.getRawRunDataByTrip();
    for (Map.Entry<Trip, RawRunData> entry : rawData.entrySet()) {
      Trip trip = entry.getKey();
      if (trip.getBlockId() == null || trip.getBlockId().length() == 0) {
        RawRunData data = entry.getValue();
        trip.setBlockId(trip.getServiceId().getId() + "_STIF_" + data.getDepotCode() + "_" + data.getBlock());
        _gtfsMutableRelationalDao.updateEntity(trip);
      }
    }
  }

  class TripWithStartTime implements Comparable<TripWithStartTime> {

    private int startTime;
    private Trip trip;

    public TripWithStartTime(Trip trip) {
      this.trip = trip;
      List<StopTime> stopTimes = _gtfsMutableRelationalDao.getStopTimesForTrip(trip);
      startTime = stopTimes.get(0).getDepartureTime();
    }

    // this is just for creating bogus objects for searching
    public TripWithStartTime(int startTime) {
      this.startTime = startTime;
    }

    @Override
    public int compareTo(TripWithStartTime o) {
      return startTime - o.startTime;
    }

    public String toString() {
      return "TripWithStartTime(" + startTime + ", " + trip + ")";
    }

  }

  @SuppressWarnings("rawtypes")
  class RawTripComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
      if (o1 instanceof Integer) {
        if (o2 instanceof Integer) {
          return ((Integer) o1) - ((Integer) o2);
        } else {
          StifTrip trip = (StifTrip) o2;
          return ((Integer) o1) - trip.listedFirstStopTime;
        }
      } else {
        if (o2 instanceof Integer) {
          return ((StifTrip) o1).listedFirstStopTime - ((Integer) o2);
        } else {
          StifTrip trip = (StifTrip) o2;
          return ((StifTrip) o1).listedFirstStopTime - trip.listedFirstStopTime;
        }
      }
    }
  }

  private void computeBlocksFromRuns(StifTripLoader loader) {

    int blockNo = 0;

    HashSet<Trip> usedGtfsTrips = new HashSet<Trip>();

    csvLogger.header("non_pullin_without_next_movement.csv", "stif_trip,stif_filename,stif_trip_record_line_num");
    csvLogger.header(
        "stif_trips_without_pullout.csv",
        "stif_trip,stif_filename,stif_trip_record_line_num,gtfs_trip_id,synthesized_block_id");
    csvLogger.header("matched_trips_gtfs_stif.csv", "agency_id,gtfs_service_id,service_id,blockId,tripId,dsc,firstStop,"+
        "firstStopTime,lastStop,lastStopTime,runId,reliefRunId,recoveryTime,firstInSeq,lastInSeq,signCodeRoute,routeId");

    Map<ServiceCode, List<StifTrip>> rawData = loader.getRawStifData();
    for (Map.Entry<ServiceCode, List<StifTrip>> entry : rawData.entrySet()) {
      List<StifTrip> rawTrips = entry.getValue();
      // this is a monster -- we want to group these by run and find the
      // pullouts
      HashMap<String, List<StifTrip>> tripsByRun = new HashMap<String, List<StifTrip>>();
      HashSet<StifTrip> unmatchedTrips = new HashSet<StifTrip>();
      ArrayList<StifTrip> pullouts = new ArrayList<StifTrip>();
      for (StifTrip trip : rawTrips) {
        String runId = trip.getRunIdWithDepot();
        List<StifTrip> byRun = tripsByRun.get(runId);
        if (byRun == null) {
          byRun = new ArrayList<StifTrip>();
          tripsByRun.put(runId, byRun);
        }
        unmatchedTrips.add(trip);
        byRun.add(trip);
        if (trip.type == StifTripType.PULLOUT) {
          pullouts.add(trip);
        }
        if (trip.type == StifTripType.DEADHEAD && 
            trip.listedFirstStopTime == trip.listedLastStopTime + trip.recoveryTime) {
          _log.warn("Zero-length deadhead.  If this immediately follows a pullout, "
              + "tracing might fail.  If it does, we will mark some trips as trips "
              + "without pullout.");
        }
      }
      for (List<StifTrip> byRun : tripsByRun.values()) {
        Collections.sort(byRun);
      }

      for (StifTrip pullout : pullouts) {
        blockNo ++;
        StifTrip lastTrip = pullout;
        int i = 0;
        HashSet<Pair<String>> blockIds = new HashSet<Pair<String>>();
        while (lastTrip.type != StifTripType.PULLIN) {

          unmatchedTrips.remove(lastTrip);
          if (++i > 200) {
            _log.warn("We seem to be caught in an infinite loop; this is usually caused\n"
                + "by two trips on the same run having the same start time.  Since nobody\n"
                + "can be in two places at once, this is an error in the STIF.  Some trips\n"
                + "will end up with missing blocks and the log will be screwed up.  A \n"
                + "representative trip starts at "
                + lastTrip.firstStop
                + " at " + lastTrip.firstStopTime + " on " + lastTrip.getRunIdWithDepot() + " on " + lastTrip.serviceCode);
            break;
          }
          // the depot may differ from the pullout depot
          String nextRunId = lastTrip.getNextRunIdWithDepot();
          if (nextRunId == null) {
            csvLogger.log("non_pullin_without_next_movement.csv", lastTrip.id, lastTrip.path, lastTrip.lineNumber); 

            _log.warn("A non-pullin has no next run; some trips will end up with missing blocks"
                    + " and the log will be messed up. The bad trip starts at " + lastTrip.firstStop + " at "
                    + lastTrip.firstStopTime + " on " + lastTrip.getRunIdWithDepot() + " on " + lastTrip.serviceCode);
            break;
          }

          List<StifTrip> trips = tripsByRun.get(nextRunId);
          if (trips == null) {
            _log.warn("No trips for run " + nextRunId);
            break;
          }

          int nextTripStartTime = lastTrip.listedLastStopTime + lastTrip.recoveryTime * 60;
          @SuppressWarnings("unchecked")
          int index = Collections.binarySearch(trips, nextTripStartTime, new RawTripComparator());

          if (index < 0) {
            index = -(index + 1);
          }
          if (index >= trips.size()) {
            _log.warn("The preceding trip says that the run "
                + nextRunId
                + " is next, but there are no trips after "
                + lastTrip.firstStopTime
                + ", so some trips will end up with missing blocks."
                + " The last trip starts at " + lastTrip.firstStop + " at "
                + lastTrip.firstStopTime + " on " + lastTrip.getRunIdWithDepot() + " on " + lastTrip.serviceCode);
            break;
          }

          StifTrip trip = trips.get(index);

          if (trip == lastTrip) {
            //we have two trips with the same start time -- usually one is a pullout of zero-length
            //we don't know if we got the first one or the last one, since Collections.binarySearch
            //makes no guarantees
            if (index > 0 && trips.get(index-1).listedFirstStopTime == nextTripStartTime) {
              index --;
              trip = trips.get(index);
            } else if (index < trips.size() - 1 && trips.get(index+1).listedFirstStopTime == nextTripStartTime) {
              index ++;
            } else {
              _log.warn("The preceding trip says that the run "
                  + nextRunId
                  + " is next, and that the next trip should start at " + nextTripStartTime
                  + ". As it happens, *this* trip starts at that time, but no other trips on"
                  + " this run do, so some trips will end up with missing blocks."
                  + " The last trip starts at " + lastTrip.firstStop + " at "
                  + lastTrip.firstStopTime + " on " + lastTrip.getRunIdWithDepot() + " on " + lastTrip.serviceCode);
              break;
            }
          }
          lastTrip = trip;
          for (Trip gtfsTrip : lastTrip.getGtfsTrips()) {
            RawRunData rawRunData = loader.getRawRunDataByTrip().get(gtfsTrip);
            String blockId;
            if (trip.agencyId.equals("MTA NYCT")) {
              blockId = gtfsTrip.getServiceId().getId() + "_" +
                  trip.serviceCode.getLetterCode() + "_" +
                  rawRunData.getDepotCode() + "_" +
                  pullout.firstStopTime + "_" +
                  pullout.runId;
            } else {
              blockId = gtfsTrip.getServiceId().getId() + "_" + trip.blockId;
            }

            blockId = blockId.intern();
            blockIds.add(Tuples.pair(blockId, gtfsTrip.getServiceId().getId()));
            gtfsTrip.setBlockId(blockId);
            _gtfsMutableRelationalDao.updateEntity(gtfsTrip);

            AgencyAndId routeId = gtfsTrip.getRoute().getId();
            addToMapSet(routeIdsByDsc, trip.getDsc(), routeId);
            dumpBlockDataForTrip(trip, gtfsTrip.getServiceId().getId(),
                gtfsTrip.getId().getId(), blockId, routeId.getId());

            usedGtfsTrips.add(gtfsTrip);
          }
          if (lastTrip.type == StifTripType.DEADHEAD) {
            for (Pair<String> blockId : blockIds) {
              String tripId = String.format("deadhead_%s_%s_%s_%s_%s", blockId.getSecond(), lastTrip.firstStop, lastTrip.firstStopTime, lastTrip.lastStop, lastTrip.runId);
              dumpBlockDataForTrip(lastTrip, blockId.getSecond(), tripId, blockId.getFirst(), "no gtfs trip");
            }
          }
        }
        unmatchedTrips.remove(lastTrip);

        for (Pair<String> blockId : blockIds) {
          String pulloutTripId = String.format("pullout_%s_%s_%s_%s", blockId.getSecond(), lastTrip.firstStop, lastTrip.firstStopTime, lastTrip.runId);
          dumpBlockDataForTrip(pullout, blockId.getSecond(), pulloutTripId , blockId.getFirst(), "no gtfs trip");
          String pullinTripId = String.format("pullin_%s_%s_%s_%s", blockId.getSecond(), lastTrip.lastStop, lastTrip.lastStopTime, lastTrip.runId);
          dumpBlockDataForTrip(lastTrip, blockId.getSecond(), pullinTripId, blockId.getFirst(), "no gtfs trip");
        }
      }

      for (StifTrip trip : unmatchedTrips) {
        _log.warn("STIF trip: " + trip + " on schedule " + entry.getKey()
            + " trip type " + trip.type
            + " must not have an associated pullout");
        for (Trip gtfsTrip : trip.getGtfsTrips()) {
          blockNo++;
          String blockId = gtfsTrip.getServiceId().getId() + "_"
              + trip.serviceCode.getLetterCode() + "_" + trip.firstStop + "_"
              + trip.firstStopTime + "_" + trip.runId.replace("-", "_")
              + blockNo + "_orphn";
          if (blockId.length() > MAX_BLOCK_ID_LENGTH) {
            blockId = truncateId(blockId);
          }
          _log.warn("Generating single-trip block id for GTFS trip: "
              + gtfsTrip.getId() + " : " + blockId);
          gtfsTrip.setBlockId(blockId);
          dumpBlockDataForTrip(trip, gtfsTrip.getServiceId().getId(),
              gtfsTrip.getId().getId(), blockId, gtfsTrip.getRoute().getId().getId());
          csvLogger.log("stif_trips_without_pullout.csv", trip.id, trip.path,
              trip.lineNumber, gtfsTrip.getId(), blockId);
          usedGtfsTrips.add(gtfsTrip);
        }
      }
    }

    HashSet<Route> routesWithTrips = new HashSet<Route>();
    csvLogger.header("gtfs_trips_with_no_stif_match.csv", "gtfs_trip_id,stif_trip");
    Collection<Trip> allTrips = _gtfsMutableRelationalDao.getAllTrips();
    for (Trip trip : allTrips) {
      if (usedGtfsTrips.contains(trip)) {
        routesWithTrips.add(trip.getRoute());
      } else {
        csvLogger.log("gtfs_trips_with_no_stif_match.csv", trip.getId(), loader.getSupport().getTripAsIdentifier(trip));
      }
    }
    
    csvLogger.header("route_ids_with_no_trips.csv", "agency_id,route_id");
    for (Route route : _gtfsMutableRelationalDao.getAllRoutes()) {
      if (routesWithTrips.contains(route)) {
        continue;
      }
      csvLogger.log("route_ids_with_no_trips.csv", route.getId().getAgencyId(), route.getId().getId());
    }
  }

  /**
   * An extremely common pattern: add an item to a set in a hash value, creating that set if
   * necessary; based on code from OTP with permission of copyright holder (OpenPlans). 
   */
  public static final <T, U> void addToMapSet(Map<T, Set<U>> mapList, T key, U value) {
      Set<U> list = mapList.get(key);
      if (list == null) {
          list = new HashSet<U>();
          mapList.put(key, list);
      }
      list.add(value);
  }


  /**
   * Dump some raw block matching data to a CSV file from stif trips
   */
  private void dumpBlockDataForTrip(StifTrip trip, String gtfsServiceId,
      String tripId, String blockId, String routeId) {

    csvLogger.log("matched_trips_gtfs_stif.csv", trip.agencyId,
        gtfsServiceId, trip.serviceCode, blockId, tripId, trip.getDsc(), trip.firstStop,
        trip.firstStopTime, trip.lastStop, trip.lastStopTime, trip.runId,
        trip.reliefRunId, trip.recoveryTime, trip.firstTripInSequence,
        trip.lastTripInSequence, trip.getSignCodeRoute(), routeId);
  }

  private void warnOnMissingTrips() {
    for (Trip t : _gtfsMutableRelationalDao.getAllTrips()) {
      String blockId = t.getBlockId();
      if (blockId == null || blockId.equals("")) {
        _log.warn("When matching GTFS to STIF, failed to find block in STIF for "
            + t.getId());
      }
    }
  }

  public void loadStif(File path, StifTripLoader loader) {
    // Exclude files and directories like .svn
    if (path.getName().startsWith("."))
      return;

    if (path.isDirectory()) {
      for (String filename : path.list()) {
        File contained = new File(path, filename);
        loadStif(contained, loader);
      }
    } else {
      loader.run(path);
    }
  }
  
  private Map<AgencyAndId, String> loadTripToDSCOverrides(String path) throws Exception {
    
    final Map<AgencyAndId, String> results = new HashMap<AgencyAndId, String>();
    
    CSVListener listener = new CSVListener() {
      
      int count = 0;
      int tripIdIndex;
      int dscIndex;
      
      @Override
      public void handleLine(List<String> line) throws Exception {
        
        if (line.size() != 2)
          throw new Exception("Each Trip ID to DSC CSV line must contain two columns.");
        
        if (count == 0) {
          count++;
          
          tripIdIndex = line.indexOf("trip_id");
          dscIndex = line.indexOf("dsc");
          
          if(tripIdIndex == -1 || dscIndex == -1) {
            throw new Exception("Trip ID to DSC CSV must contain a header with column names 'trip_id' and 'dsc'.");
          }
          
          return;
        }
        
        results.put(AgencyAndIdLibrary.convertFromString(line.get(tripIdIndex)), line.get(dscIndex));
      }
    };
    
    File source = new File(path);
    
    new CSVLibrary().parse(source, listener);
    
    return results;
  }
  
  private void readNotInServiceDscs() {
    if (_notInServiceDscPath != null) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(
            _notInServiceDscPath));
        String line = null;
        while ((line = reader.readLine()) != null)
          _notInServiceDscs.add(line);
      } catch (IOException ex) {
        throw new IllegalStateException("unable to read nonInServiceDscPath: "
            + _notInServiceDscPath);
      }
    }
  }

  /**
   * Whether blocks should come be computed from runs (true) or read from the STIF (false)
   * @return
   */
  public boolean usesFallBackToStifBlocks() {
    return fallBackToStifBlocks;
  }

  public void setFallBackToStifBlocks(boolean fallBackToStifBlocks) {
    this.fallBackToStifBlocks = fallBackToStifBlocks;
  }

  // for unit tests
  public void setStifTripLoader(StifTripLoader loader) {
    _loader = loader;
  }
  
  // for unit tests
  public void setCSVLogger(MultiCSVLogger logger) {
    this.csvLogger = logger;
  }
  
  // package private for unit tests
  String truncateId(String id) {
    if (id == null) return null;
    return id.replaceAll("[aeiouy\\s]", "");
  }
}
