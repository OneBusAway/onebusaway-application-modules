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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.transit_data.model.oba.RunData;
import org.onebusaway.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.EventRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.GeographyRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.ServiceCode;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.SignCodeRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.StifRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.TimetableRecord;
import org.onebusaway.transit_data_federation.bundle.tasks.stif.model.TripRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Create a mapping from Destination Sign Code (DSC) to GTFS Trip objects using
 * data in STIF, MTA's internal format.
 */
public class StifTripLoader {

  private static final Logger _log = LoggerFactory.getLogger(StifTripLoader.class);

  private StifTripLoaderSupport support = new StifTripLoaderSupport();

  private Map<String, List<AgencyAndId>> tripIdsBySignCode = new HashMap<String, List<AgencyAndId>>();

  private int _tripsCount = 0;

  private int _tripsWithoutMatchCount = 0;

  private Map<AgencyAndId, GeographyRecord> geographyRecordsByBoxId = new HashMap<AgencyAndId, GeographyRecord>();

  private Map<AgencyAndId, RunData> runsForTrip = new HashMap<AgencyAndId, RunData>();

  private Map<Trip, RawRunData> rawRunDataByTrip = new HashMap<Trip, RawRunData>();

  private Map<ServiceCode, List<StifTrip>> rawData = new HashMap<ServiceCode, List<StifTrip>>();
  
  private Map<AgencyAndId, List<NonRevenueStopData>> nonRevenueStopDataByTripId = new HashMap<AgencyAndId, List<NonRevenueStopData>>();

  private MultiCSVLogger csvLogger;

  private Pattern oldNYCTServiceIdFormat = Pattern.compile(".*[0-9]{8}[A-Z]{2}$");
  
  private Map<DuplicateTripCheckKey, StifTrip> tripsByRunAndStartTime = new HashMap<DuplicateTripCheckKey, StifTrip>();

  class DuplicateTripCheckKey {
    public DuplicateTripCheckKey(String runId, int startTime,
        ServiceCode serviceCode) {
      this.runId = runId;
      this.startTime = startTime;
      this.serviceCode = serviceCode;
    }

    String runId;
    Integer startTime;
    ServiceCode serviceCode;
  }

  @Autowired
  public void setGtfsDao(GtfsMutableRelationalDao dao) {
    support.setGtfsDao(dao);
  }

  /**
   * Get the mapping from DSC and schedule id to list of trips.
   */
  public Map<String, List<AgencyAndId>> getTripMapping() {
    return tripIdsBySignCode;
  }

  public Map<AgencyAndId, GeographyRecord> getGeographyRecordsByBoxId() {
    return geographyRecordsByBoxId;
  }
  
  public int getTripsCount() {
    return _tripsCount;
  }

  public int getTripsWithoutMatchCount() {
    return _tripsWithoutMatchCount;
  }

  /**
   * For each STIF file, call run().
   */
  public void run(File path) {
    try {
      _log.info("loading stif from " + path.getAbsolutePath());
      InputStream in = new FileInputStream(path);
      if (path.getName().endsWith(".gz"))
        in = new GZIPInputStream(in);
      run(in, path);
    } catch (Exception e) {
      throw new RuntimeException("Error loading " + path, e);
    }
  }

  public void run(InputStream stream, File path) {
    StifRecordReader reader;

    boolean warned = false;
    int lineNumber = 0;
    int tripLineNumber = 0;
    TripRecord tripRecord = null;
    EventRecord eventRecord = null;
    EventRecord firstEventRecord = null;
    EventRecord firstNonRevEventRecord = null;
    EventRecord lastNonRevEventRecord = null;
    try {
      reader = new StifRecordReader(stream);
      ServiceCode serviceCode = null;
      String agencyId = null;
      boolean isBusCo = false;
      while (true) {
        StifRecord record = reader.read();
        lineNumber++;

        if (record == null) {
          break;
        }
        if (record instanceof TimetableRecord) {
          TimetableRecord timetableRecord = (TimetableRecord) record;
          serviceCode = timetableRecord.getServiceCode();
          agencyId = timetableRecord.getAgencyId();
          isBusCo = "MTABC".equals(agencyId);
          if (!rawData.containsKey(serviceCode)) {
            rawData.put(serviceCode, new ArrayList<StifTrip>());
          }
          continue;
        }
        if (record instanceof GeographyRecord) {
          GeographyRecord geographyRecord = ((GeographyRecord) record);
          geographyRecordsByBoxId.put(new AgencyAndId(agencyId, geographyRecord.getBoxID()), 
        		  geographyRecord);
          support.putStopIdForLocation(geographyRecord.getIdentifier(),
              geographyRecord.getBoxID());
          continue;
        }

        if (record instanceof EventRecord) {
          // track the first and last event records which are revenue stops
          // for use in trip processing
          if (tripRecord == null) {
            continue;
          }
          EventRecord possibleEventRecord = (EventRecord) record;
          if (possibleEventRecord.getLocation() == null)
            //yet another new kind of bogus record
            continue;
          if (!possibleEventRecord.isRevenue()) {
            // Keep track of the first and last non-revenue stop events
            lastNonRevEventRecord = possibleEventRecord;
            if (firstNonRevEventRecord == null && firstEventRecord == null) {
              firstNonRevEventRecord = lastNonRevEventRecord;
              lastNonRevEventRecord = null;
            }
            continue;
          }
          eventRecord = possibleEventRecord;
          if (firstEventRecord == null) {
            firstEventRecord = eventRecord;
          }
        }

        if (record instanceof TripRecord || record instanceof SignCodeRecord) {
          // the SignCodeRecord is guaranteed by spec to be after all
          // TripRecords
          // So, for each trip's worth of event records, a SignCode record or a
          // TripRecord will follow.
          // this code parses the trip record whose events end on the line
          // *before* this.
          if (tripRecord == null) {
            // we have already finished with this trip
            if (record instanceof TripRecord) {
              // prepare for next trip
              tripLineNumber = lineNumber;
              tripRecord = (TripRecord) record;
              firstNonRevEventRecord = lastNonRevEventRecord = null;
              eventRecord = firstEventRecord = null;
            } else {
              tripRecord = null;
            }
            continue;
          }
          int tripType = tripRecord.getTripType();
          
          boolean fakeDeadhead = false;
          if (firstEventRecord == null && !(tripType == 2 || tripType == 3 || tripType == 4 )) {
            //revenue trips must have at least one revenue stop
            _log.warn("Revenue trip at " + tripLineNumber + " in " + path + " has no revenue stops.  " +
                "Using first/last stops from trip layer rather than event layer.");
              fakeDeadhead = true;
          }
          
          if (tripType == 2 || tripType == 3 || tripType == 4 || fakeDeadhead) {
            // this must be a non-revenue trip
            if (firstEventRecord != null) {
              //non-revenue trips should have no revenue stops
              _log.warn("Non-revenue trip at " + tripLineNumber + " in " + path + " has a revenue stop");
            }

            StifTrip stifTrip = new StifTrip(tripRecord.getRunId(),
                tripRecord.getReliefRunId(),
                tripRecord.getNextTripOperatorRunId(),
                StifTripType.byValue(tripType), tripRecord.getSignCode());
            stifTrip.agencyId = agencyId;
            stifTrip.serviceCode = serviceCode;
            stifTrip.depot = tripRecord.getDepotCode();
            if (tripRecord.getNextTripOperatorDepotCode() != null) {
              stifTrip.nextTripOperatorDepot = tripRecord.getNextTripOperatorDepotCode();
            } else {
              stifTrip.nextTripOperatorDepot = tripRecord.getDepotCode(); 
            }
            stifTrip.firstStopTime = tripRecord.getOriginTime();
            stifTrip.lastStopTime = tripRecord.getDestinationTime();
            stifTrip.listedFirstStopTime = tripRecord.getOriginTime();
            stifTrip.listedLastStopTime = tripRecord.getDestinationTime();
            stifTrip.firstStop = support.getStopIdForLocation(tripRecord.getOriginLocation());
            stifTrip.lastStop = support.getStopIdForLocation(tripRecord.getDestinationLocation());
            stifTrip.recoveryTime = tripRecord.getRecoveryTime();
            stifTrip.firstTripInSequence = tripRecord.isFirstTripInSequence();
            stifTrip.lastTripInSequence = tripRecord.isLastTripInSequence();
            stifTrip.signCodeRoute = tripRecord.getSignCodeRoute();
            stifTrip.path = path;
            stifTrip.lineNumber = tripLineNumber;
            stifTrip.blockId = tripRecord.getBlockNumber();
            rawData.get(serviceCode).add(stifTrip);

            if (record instanceof TripRecord) {
              tripLineNumber = lineNumber;
              tripRecord = (TripRecord) record;
              firstNonRevEventRecord = lastNonRevEventRecord = null;
              eventRecord = firstEventRecord = null;
            } else {
              tripRecord = null;
            }
            continue;

          }

          String runId = tripRecord.getRunIdWithDepot();
          String reliefRunId = tripRecord.getReliefRunId();
          String nextOperatorRunId = tripRecord.getNextTripOperatorRunIdWithDepot();

          StifTrip stifTrip = new StifTrip(tripRecord.getRunId(),
              tripRecord.getReliefRunId(),
              tripRecord.getNextTripOperatorRunId(),
              StifTripType.byValue(tripType), tripRecord.getSignCode());
          stifTrip.agencyId = agencyId;
          stifTrip.serviceCode = serviceCode;
          stifTrip.depot = tripRecord.getDepotCode();
          if (tripRecord.getNextTripOperatorDepotCode() != null) {
            stifTrip.nextTripOperatorDepot = tripRecord.getNextTripOperatorDepotCode();
          } else {
            stifTrip.nextTripOperatorDepot = tripRecord.getDepotCode(); 
          }
          stifTrip.firstStopTime = firstEventRecord.getTime();
          stifTrip.lastStopTime = eventRecord.getTime();
          stifTrip.firstStop = support.getStopIdForLocation(firstEventRecord.getLocation());
          stifTrip.lastStop = support.getStopIdForLocation(eventRecord.getLocation());
          stifTrip.listedFirstStopTime = tripRecord.getOriginTime();
          stifTrip.listedLastStopTime = tripRecord.getDestinationTime();
          stifTrip.recoveryTime = tripRecord.getRecoveryTime();
          stifTrip.firstTripInSequence = tripRecord.isFirstTripInSequence();
          stifTrip.lastTripInSequence = tripRecord.isLastTripInSequence();
          stifTrip.signCodeRoute = tripRecord.getSignCodeRoute();
          stifTrip.path = path;
          stifTrip.lineNumber = tripLineNumber;
          stifTrip.blockId = tripRecord.getBlockNumber();
          rawData.get(serviceCode).add(stifTrip);

          String destSignCode = tripRecord.getSignCode();

          TripIdentifier tripIdentifier = support.getIdentifierForStifTrip(tripRecord,
              stifTrip);
          stifTrip.id = tripIdentifier;

          if (stifTrip.signCodeRoute == null
              || stifTrip.signCodeRoute.length() == 0) {
            csvLogger.log("stif_trip_layers_with_missing_route.csv", tripIdentifier, path,
                tripLineNumber, "signCodeRoute");
          }
          if (stifTrip.getDsc() == null || stifTrip.getDsc().length() == 0) {
            csvLogger.log("stif_trip_layers_with_missing_route.csv", tripIdentifier, path,
                tripLineNumber, "DSC");
          }

          DuplicateTripCheckKey key = new DuplicateTripCheckKey(stifTrip.runId,
              stifTrip.firstStopTime, stifTrip.serviceCode);
          StifTrip oldTrip = tripsByRunAndStartTime.get(key);
          if (oldTrip == null) {
            tripsByRunAndStartTime.put(key, stifTrip);
          } else {
            csvLogger.log("trips_with_duplicate_run_and_start_time.csv", tripIdentifier,
                stifTrip.path, stifTrip.lineNumber, oldTrip.id, oldTrip.path,
                oldTrip.lineNumber);
          }

          List<Trip> trips = support.getTripsForIdentifier(tripIdentifier);

          _tripsCount++;

          if (trips == null || trips.isEmpty()) {
            csvLogger.log("stif_trips_with_no_gtfs_match.csv", tripIdentifier, path,
                tripLineNumber);

            // trip in stif but not in gtfs
            if (!warned) {
              warned = true;
              _log.warn("gtfs trip not found for " + tripIdentifier);
            }
            _tripsWithoutMatchCount++;
            if (record instanceof TripRecord) {
              // prepare for next trip
              tripLineNumber = lineNumber;
              tripRecord = (TripRecord) record;
              firstNonRevEventRecord = lastNonRevEventRecord = null;
              eventRecord = firstEventRecord = null;
            } else {
              tripRecord = null;
            }
            continue;
          }

          List<Trip> filtered = new ArrayList<Trip>();
          /* filter trips by schedule or (for MTABC) GTFS trip ID */
          for (Trip gtfsTrip : trips) {
            if (gtfsTrip.getId().getId().equals(tripRecord.getGtfsTripId())) {
              addGtfsTrip(path, tripLineNumber, tripRecord, runId, reliefRunId, nextOperatorRunId,
                  stifTrip, destSignCode, tripIdentifier, filtered, gtfsTrip);
              continue;
            }

            String serviceId = gtfsTrip.getServiceId().getId();
            Matcher m = oldNYCTServiceIdFormat.matcher(serviceId);
            boolean isNewMTAHastusGtfsFormat = !m.matches();
            
            if (isBusCo || isNewMTAHastusGtfsFormat) {
              ServiceCode tripServiceCode = ServiceCode.getServiceCodeForMTAHastusGTFS(serviceId);              
              if (serviceCode != tripServiceCode) {
                  gtfsTrip = null;
              }
            	
            // legacy NYCT format, where service ID ends in primary/secondary schedule 
            } else {
              /*
               * Service codes are of the form 20100627CA Only the last two
               * characters are important. They have the meaning: A = sat B =
               * weekday closed C = weekday open D = sun
               * 
               * The first character is for trips on that day's STIF schedule,
               * while the second character is for trips on the next day's STIF
               * schedule (but that run on that day).
               * 
               * To figure out whether a GTFS trip corresponds to a STIF trip, if
               * the STIF trip is before midnight, check daycode1; else check
               * daycode2
               */
              Character dayCode1 = serviceId.charAt(serviceId.length() - 2);
              Character dayCode2 = serviceId.charAt(serviceId.length() - 1);

              if (stifTrip.firstStopTime < 0) {
                // possible trip records are those containing the previous day
                if (StifTripLoaderSupport.scheduleIdForGtfsDayCode(dayCode2.toString()) != serviceCode) {
                  gtfsTrip = null;
                }
              } else {
                if (StifTripLoaderSupport.scheduleIdForGtfsDayCode(dayCode1.toString()) != serviceCode) {
                  gtfsTrip = null;
                }
              }
            }
            if (gtfsTrip != null) {
              addGtfsTrip(path, tripLineNumber, tripRecord, runId, reliefRunId, nextOperatorRunId,
                  stifTrip, destSignCode, tripIdentifier, filtered, gtfsTrip);
            }
          }
          if (filtered.size() == 0) {
            if (stifTrip.type == StifTripType.REVENUE
                && (destSignCode == null || destSignCode.length() == 0)) {
              _log.warn("Revenue trip " + stifTrip + " did not have a DSC");
              csvLogger.log("trips_with_null_dscs.csv", "(no GTFS trips)", tripIdentifier,
                  path, tripLineNumber);
            }
          }

          List<AgencyAndId> sctrips = tripIdsBySignCode.get(destSignCode);
          if (sctrips == null) {
            sctrips = new ArrayList<AgencyAndId>();
            tripIdsBySignCode.put(destSignCode, sctrips);
          }
          for (Trip trip : filtered) {
            sctrips.add(trip.getId());
            
            // Insert non revenue stop data into a map that can be serialized later
            if (firstNonRevEventRecord != null || lastNonRevEventRecord != null) {
              if (!nonRevenueStopDataByTripId.containsKey(trip.getId())) {
                nonRevenueStopDataByTripId.put(trip.getId(), new ArrayList<NonRevenueStopData>());
              }
              if (firstNonRevEventRecord != null) {
                NonRevenueStopData firstNonRevenueStopData = new NonRevenueStopData();
                firstNonRevenueStopData.setNonRevenueStopOrder(NonRevenueStopOrder.FIRST);
                firstNonRevenueStopData.setScheduleTime(firstNonRevEventRecord.getTime());
                String stopId = support.getStopIdForLocation(firstNonRevEventRecord.getLocation());
                AgencyAndId stopAgencyAndId = new AgencyAndId(agencyId, stopId);
                GeographyRecord geographyRecord = geographyRecordsByBoxId.get(stopAgencyAndId);
                Coordinate c = new Coordinate(geographyRecord.getLongitude(), geographyRecord.getLatitude());
                firstNonRevenueStopData.setLocation(c);
                nonRevenueStopDataByTripId.get(trip.getId()).add(firstNonRevenueStopData);
              }
              if (lastNonRevEventRecord != null) {
                NonRevenueStopData lastNonRevenueStopData = new NonRevenueStopData();
                lastNonRevenueStopData.setNonRevenueStopOrder(NonRevenueStopOrder.LAST);
                lastNonRevenueStopData.setScheduleTime(lastNonRevEventRecord.getTime());
                String stopId = support.getStopIdForLocation(lastNonRevEventRecord.getLocation());
                AgencyAndId stopAgencyAndId = new AgencyAndId(agencyId, stopId);
                GeographyRecord geographyRecord = geographyRecordsByBoxId.get(stopAgencyAndId);
                Coordinate c = new Coordinate(geographyRecord.getLongitude(), geographyRecord.getLatitude());
                lastNonRevenueStopData.setLocation(c);
                nonRevenueStopDataByTripId.get(trip.getId()).add(lastNonRevenueStopData);
              }
            }
          }
          tripRecord = null; // we are done processing this trip record
        }
        if (record instanceof TripRecord) {
          tripLineNumber = lineNumber;
          tripRecord = (TripRecord) record;
          firstNonRevEventRecord = lastNonRevEventRecord = null;
          eventRecord = firstEventRecord = null;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addGtfsTrip(File path, int tripLineNumber,
      TripRecord tripRecord, String runId, String reliefRunId, String nextOperatorRunId,
      StifTrip rawTrip, String code, TripIdentifier id, List<Trip> filtered,
      Trip trip) {
    rawTrip.addGtfsTrip(trip);

    int reliefTime = tripRecord.getReliefTime();
    String block = tripRecord.getBlockNumber();
    String depotCode = tripRecord.getDepotCode();
    RawRunData rawRunData = new RawRunData(runId, reliefRunId, nextOperatorRunId,
        block, depotCode);

    filtered.add(trip);
    rawRunDataByTrip.put(trip, rawRunData);
    runsForTrip.put(trip.getId(), new RunData(runId, reliefRunId, reliefTime));

    if (rawTrip.type == StifTripType.REVENUE
        && (code == null || code.length() == 0)) {
      _log.warn("Revenue trip " + rawTrip + " did not have a DSC");
      csvLogger.log("trips_with_null_dscs.csv", trip.getId(), id,
          path, tripLineNumber);
    }
  }

  public Map<Trip, RawRunData> getRawRunDataByTrip() {
    return rawRunDataByTrip;
  }

  public Map<AgencyAndId, RunData> getRunsForTrip() {
    return runsForTrip;
  }

  public Map<ServiceCode, List<StifTrip>> getRawStifData() {
    return rawData;
  }
  
  public Map<AgencyAndId, List<NonRevenueStopData>> getNonRevenueStopDataByTripId() {
    return nonRevenueStopDataByTripId;
  }

  public void setLogger(MultiCSVLogger csvLogger) {
    this.csvLogger = csvLogger;
    csvLogger.header("trips_with_null_dscs.csv",
        "gtfs_trip_id,stif_trip,stif_filename,stif_trip_record_line_num");
    csvLogger.header("stif_trips_with_no_gtfs_match.csv",
        "stif_trip,stif_filename,stif_trip_record_line_num");
    csvLogger.header("stif_trip_layers_with_missing_route.csv",
        "stif_trip,stif_filename,stif_trip_record_line_num,missing_field");
    csvLogger.header(
        "trips_with_duplicate_run_and_start_time.csv",
        "stif_trip1,stif_filename1,stif_trip_record_line_num1,stif_trip2,stif_filename2,stif_trip_record_line_num2");

  }

  public StifTripLoaderSupport getSupport() {
    return support;
  }

  public void setSupport(StifTripLoaderSupport support) {
    this.support = support;
  }
}
