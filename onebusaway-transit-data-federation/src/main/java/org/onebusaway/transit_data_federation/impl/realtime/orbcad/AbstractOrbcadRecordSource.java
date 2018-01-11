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
package org.onebusaway.transit_data_federation.impl.realtime.orbcad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationListener;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredDataSource;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.MonitoredResult;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocationService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource("org.onebusaway.transit_data_federation.impl.realtime.orbcad:name=OrbcadRecordFtpSource")
public abstract class AbstractOrbcadRecordSource implements MonitoredDataSource {

  private static final int REFRESH_INTERVAL_IN_SECONDS = 30;

  private static Logger _log = LoggerFactory.getLogger(AbstractOrbcadRecordSource.class);

  protected ScheduledExecutorService _executor = Executors.newSingleThreadScheduledExecutor();

  private List<VehicleLocationRecord> _records = new ArrayList<VehicleLocationRecord>();

  private int _refreshInterval = REFRESH_INTERVAL_IN_SECONDS;

  private long _lastRefresh = 0;

  private VehicleLocationListener _vehicleLocationListener;

  private Map<String, List<String>> _blockIdMapping = new HashMap<String, List<String>>();

  private BlockCalendarService _blockCalendarService;

  private ScheduledBlockLocationService _scheduledBlockLocationService;

  protected List<String> _agencyIds;

  private File _blockIdMappingFile;

  private double _motionThreshold = 30;

  /****
   * 
   ****/

  private Map<AgencyAndId, VehicleLocationRecord> _lastRecordByVehicleId = new HashMap<AgencyAndId, VehicleLocationRecord>();

  /****
   * Statistics
   ****/

  private transient int _recordsTotal = 0;

  private transient int _recordsWithoutScheduleDeviation = 0;

  private transient int _recordsWithoutBlockId = 0;

  private transient int _recordsWithoutBlockIdInGraph = 0;

  private transient int _recordsWithoutServiceDate = 0;

  private transient int _recordsValid = 0;
  
  private transient long _latestUpdate = 0;
  
  private MonitoredResult _monitoredResult, _currentResult = new MonitoredResult();
  
  private String _feedId = null;

  public void setRefreshInterval(int refreshIntervalInSeconds) {
    _refreshInterval = refreshIntervalInSeconds;
  }

  public void setAgencyId(String agencyId) {
    _agencyIds = Arrays.asList(agencyId);
  }

  public void setAgencyIds(List<String> agencyIds) {
    _agencyIds = agencyIds;
  }
  
  public List<String> getAgencyIds() {
    return _agencyIds;
  }

  public void setBlockIdMappingFile(File blockIdMappingFile) {
    _blockIdMappingFile = blockIdMappingFile;
  }

  @Autowired
  public void setVehicleLocationListener(
      VehicleLocationListener vehicleLocationListener) {
    _vehicleLocationListener = vehicleLocationListener;
  }

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Autowired
  public void setScheduledBlockLocationService(
      ScheduledBlockLocationService scheduledBlockLocationService) {
    _scheduledBlockLocationService = scheduledBlockLocationService;
  }

  public MonitoredResult getMonitoredResult() {
    return _monitoredResult;
  }
  
  public String getFeedId() {
    return _feedId;
  }
  
  public void setFeedId(String id) {
    _feedId = id;
  }
  
  /****
   * JMX Attributes
   ***/

  @ManagedAttribute
  public int getRecordsTotal() {
    return _recordsTotal;
  }

  @ManagedAttribute
  public int getRecordWithoutScheduleDeviation() {
    return _recordsWithoutScheduleDeviation;
  }

  @ManagedAttribute
  public int getRecordsWithoutBlockId() {
    return _recordsWithoutBlockId;
  }

  @ManagedAttribute
  public int getRecordsWithoutBlockIdInGraph() {
    return _recordsWithoutBlockIdInGraph;
  }

  @ManagedAttribute
  public int getRecordsWithoutServiceDate() {
    return _recordsWithoutServiceDate;
  }

  @ManagedAttribute
  public int getRecordsValid() {
    return _recordsValid;
  }

  
  
  /****
   * Setup and Teardown
   ****/

  protected void start() throws SocketException, IOException {

    loadBlockIdMapping();

    setup();

    _executor.scheduleAtFixedRate(new AvlRefreshTask(), 5,
        _refreshInterval / 2, TimeUnit.SECONDS);
  }

  protected void stop() throws IOException {
    _executor.shutdown();
  }

  /****
   * Protected Methods
   ****/

  protected void setup() {

  }

  protected abstract void handleRefresh() throws IOException;

  /****
   * Private Methods
   ****/

  private void loadBlockIdMapping() throws FileNotFoundException, IOException {

    try {

      if (_blockIdMappingFile == null)
        return;

      BufferedReader reader = new BufferedReader(new FileReader(
          _blockIdMappingFile));
      String line = null;

      while ((line = reader.readLine()) != null) {
        int index = line.indexOf(',');
        String from = line.substring(0, index);
        String to = line.substring(index + 1);
        List<String> toValues = _blockIdMapping.get(from);
        if (toValues == null) {
          toValues = new ArrayList<String>();
          _blockIdMapping.put(from, toValues);
        }
        toValues.add(to);
      }

      reader.close();

    } catch (Throwable ex) {
      _log.warn("error loading block id mapping from file " + _blockIdMapping,
          ex);
    }
  }

  private BlockInstance getBlockInstanceForRecord(OrbcadRecord record) {

    long recordTime = record.getTime() * 1000;
    long timeFrom = recordTime - 30 * 60 * 1000;
    long timeTo = recordTime + 30 * 60 * 1000;

    List<AgencyAndId> blockIds = getBlockIdsForRecord(record);

    List<BlockInstance> allInstances = new ArrayList<BlockInstance>();

    for (AgencyAndId blockId : blockIds) {
      List<BlockInstance> instances = _blockCalendarService.getActiveBlocks(
          blockId, timeFrom, timeTo);
      allInstances.addAll(instances);
    }

    // TODO : We currently assume we don't have overlapping blocks.
    if (allInstances.size() != 1)
      return null;

    return allInstances.get(0);
  }

  private List<AgencyAndId> getBlockIdsForRecord(OrbcadRecord record) {

    List<AgencyAndId> blockIds = new ArrayList<AgencyAndId>();

    String rawBlockId = Integer.toString(record.getBlock());
    List<String> rawBlockIds = _blockIdMapping.get(rawBlockId);
    if (rawBlockIds == null)
      rawBlockIds = Arrays.asList(rawBlockId);

    for (String agencyId : _agencyIds) {
      for (String rawId : rawBlockIds) {
        blockIds.add(new AgencyAndId(agencyId, rawId));
      }
    }

    return blockIds;
  }

  protected class AvlRefreshTask implements Runnable {

    public void run() {
      try {

        _log.debug("checking if we need to refresh");

        synchronized (this) {
          long t = SystemTime.currentTimeMillis();
          if (_lastRefresh + _refreshInterval * 1000 > t)
            return;
          _lastRefresh = t;
        }

        _log.debug("refresh requested");

        preHandleRefresh();
        handleRefresh();
        _currentResult.setLastUpdate(_latestUpdate);
        postHandleRefresh();

        try {
          _vehicleLocationListener.handleVehicleLocationRecords(_records);
        } catch (Throwable ex) {
          _log.warn("error passing schedule adherence records to listener", ex);
        }

        _records.clear();

        _log.debug("refresh complete");

      } catch (Throwable ex) {
        _log.warn("error refreshing data", ex);
      }
    }

    private void preHandleRefresh() {
      _currentResult = new MonitoredResult();
      _currentResult.setAgencyIds(_agencyIds);
    }

    
    private void postHandleRefresh() {
      _log.info("Agencies " + _agencyIds + " have active vehicles=" +_currentResult.getMatchedTripIds().size()
              + " for updates=" + _currentResult.getRecordsTotal()
              + "  with most recent timestamp " + new Date(_latestUpdate));
      if (_currentResult.getRecordsTotal() > 0) {
        // only consider it a successful update if we got some records
        // ftp impl may not have a new file to download
        _monitoredResult = _currentResult;
      }
    }


  }

  protected class RecordHandler implements EntityHandler {

    @Override
    public void handleEntity(Object bean) {

      OrbcadRecord record = (OrbcadRecord) bean;

      _recordsTotal++;
      _currentResult.addRecordTotal();
      

      if (!record.hasScheduleDeviation()) {
        _recordsWithoutScheduleDeviation++;
        return;
      }

      if (record.getBlock() == 0) {
        _recordsWithoutBlockId++;
        return;
      }

      BlockInstance blockInstance = getBlockInstanceForRecord(record);
      if (blockInstance == null) {
        _recordsWithoutServiceDate++;
        return;
      }

      BlockConfigurationEntry blockConfig = blockInstance.getBlock();
      BlockEntry block = blockConfig.getBlock();
      AgencyAndId blockId = block.getId();

      VehicleLocationRecord message = new VehicleLocationRecord();

      message.setBlockId(blockId);

      message.setServiceDate(blockInstance.getServiceDate());
      message.setTimeOfRecord(record.getTime() * 1000);
      if (message.getTimeOfRecord() > _latestUpdate) {
        _latestUpdate = message.getTimeOfRecord();
      }
      message.setTimeOfLocationUpdate(message.getTimeOfRecord());
      
      // In Orbcad, +scheduleDeviation means the bus is early and -schedule
      // deviation means bus is late, which is opposite the
      // ScheduleAdherenceRecord convention
      message.setScheduleDeviation(-record.getScheduleDeviation());

      message.setVehicleId(new AgencyAndId(blockId.getAgencyId(),
          Integer.toString(record.getVehicleId())));

      if (record.hasLat() && record.hasLon()) {
        message.setCurrentLocationLat(record.getLat());
        message.setCurrentLocationLon(record.getLon());
        _currentResult.addLatLon(record.getLat(), record.getLon());
      }

      int effectiveScheduleTime = (int) (record.getTime() - blockInstance.getServiceDate() / 1000);
      int adjustedScheduleTime = effectiveScheduleTime
          - record.getScheduleDeviation();

      ScheduledBlockLocation location = _scheduledBlockLocationService.getScheduledBlockLocationFromScheduledTime(
          blockConfig, adjustedScheduleTime);

      if (location != null) {
        message.setDistanceAlongBlock(location.getDistanceAlongBlock());

        BlockTripEntry activeTrip = location.getActiveTrip();
        if (activeTrip != null) {
          message.setTripId(activeTrip.getTrip().getId());

          addTimepointRecords(message, activeTrip, blockInstance.getServiceDate());
          _currentResult.addMatchedTripId(activeTrip.getTrip().getId().toString());
        } else {
          _currentResult.addUnmatchedTripId(blockId.toString()); // this isn't exactly right
        }
        // Are we at the start of the block?
        if (location.getDistanceAlongBlock() == 0) {

          VehicleLocationRecord lastRecord = _lastRecordByVehicleId.get(message.getVehicleId());
          boolean inMotion = true;
          if (lastRecord != null && lastRecord.isCurrentLocationSet()
              && message.isCurrentLocationSet()) {
            double d = SphericalGeometryLibrary.distance(
                lastRecord.getCurrentLocationLat(),
                lastRecord.getCurrentLocationLon(),
                message.getCurrentLocationLat(),
                message.getCurrentLocationLon());
            inMotion = d > _motionThreshold;
          }

          if (inMotion)
            message.setPhase(EVehiclePhase.DEADHEAD_BEFORE);
          else
            message.setPhase(EVehiclePhase.LAYOVER_BEFORE);
        }
      }

      _records.add(message);
      _recordsValid++;

      _lastRecordByVehicleId.put(message.getVehicleId(), message);
    }

    private void addTimepointRecords(VehicleLocationRecord message, BlockTripEntry activeTrip, long serviceDate) {
      if (activeTrip == null) {
        return;
      }

      if (activeTrip.getStopTimes() == null) {
        return;
      }
      List<BlockStopTimeEntry> stopTimes = activeTrip.getStopTimes();

      for (BlockStopTimeEntry stopTime : stopTimes) {
        long scheduleTime = serviceDate + stopTime.getStopTime().getDepartureTime() * 1000;
        long now = message.getTimeOfRecord();
        long delay = (long)message.getScheduleDeviation();
        long predictedTime = delay * 1000 + scheduleTime;

        // only create records for predictions in the future
        if (predictedTime >= now) {
          TimepointPredictionRecord tpr = new TimepointPredictionRecord();
          tpr.setTimepointId(stopTime.getStopTime().getStop().getId());
          tpr.setTripId(activeTrip.getTrip().getId());
          tpr.setTimepointScheduledTime(scheduleTime);
          // propagate delay across the trip as a trivial prediction
          tpr.setTimepointPredictedArrivalTime(predictedTime);
          tpr.setTimepointPredictedDepartureTime(predictedTime);
          if (message.getTimepointPredictions() == null) {
            message.setTimepointPredictions(new ArrayList<TimepointPredictionRecord>());
          }
          message.getTimepointPredictions().add(tpr);
        }

      }

    }
  }
}
