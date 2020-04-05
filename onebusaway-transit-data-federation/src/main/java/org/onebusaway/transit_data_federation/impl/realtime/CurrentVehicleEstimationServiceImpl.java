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
package org.onebusaway.transit_data_federation.impl.realtime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean.Record;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.impl.probability.DeviationModel;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockCalendarService;
import org.onebusaway.transit_data_federation.services.blocks.BlockGeospatialService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockSequenceIndex;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationService;
import org.onebusaway.transit_data_federation.services.realtime.CurrentVehicleEstimationService;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

@Component
class CurrentVehicleEstimationServiceImpl implements
    CurrentVehicleEstimationService {

  private static NumberFormat _format = new DecimalFormat("0.00");

  private BlockCalendarService _blockCalendarService;

  private BlockGeospatialService _blockGeospatialService;

  private BlockStatusService _blockStatusService;

  private TripDetailsBeanService _tripStatusBeanService;

  private BlockLocationService _blockLocationService;

  @Autowired
  public void setBlockCalendarService(BlockCalendarService blockCalendarService) {
    _blockCalendarService = blockCalendarService;
  }

  @Autowired
  public void setBlockGeospatialService(
      BlockGeospatialService blockGeospatialService) {
    _blockGeospatialService = blockGeospatialService;
  }

  @Autowired
  public void setBlockStatusService(BlockStatusService blockStatusService) {
    _blockStatusService = blockStatusService;
  }

  @Autowired
  public void setTripStatusBeanService(
      TripDetailsBeanService tripStatusBeanService) {
    _tripStatusBeanService = tripStatusBeanService;
  }

  @Autowired
  public void setBlockLocationService(BlockLocationService blockLocationService) {
    _blockLocationService = blockLocationService;
  }

  /**
   * If a record is older than max window (in minutes), we don't consider it
   */
  private int _maxWindow = 5;

  /**
   * The location accuracy must be at least this good (in meters) for us to
   * consider the record
   */
  private double _maxAccuracy = 250;

  private DeviationModel _realTimeLocationDeviationModel = new DeviationModel(
      200);

  private DeviationModel _scheduleOnlyLocationDeviationModel = new DeviationModel(
      500);

  private DeviationModel _scheduleDeviationLateModel = new DeviationModel(
      15 * 60);

  private DeviationModel _scheduleDeviationEarlyModel = new DeviationModel(
      4 * 60);

  private int _maxTravelBackwardsTime = 2 * 60;

  /**
   * For every minute between two location updates, the amount of backwards
   * travel time is reduced by this much
   */
  private double _maxTravelBackwardsDecayFactor = 0.4;

  private double _shortCutProbability = 0.5;

  @Override
  public ListBean<CurrentVehicleEstimateBean> getCurrentVehicleEstimates(
      CurrentVehicleEstimateQueryBean query) {

    long minT = SystemTime.currentTimeMillis() - _maxWindow * 60 * 1000;
    minT = 0;

    List<Record> records = getRecords(query.getRecords(), minT);

    if (records.isEmpty())
      return new ListBean<CurrentVehicleEstimateBean>();

    List<CurrentVehicleEstimateBean> beans = new ArrayList<CurrentVehicleEstimateBean>();

    if (tryDirectMatchAgainstVehicleId(query, records, beans))
      return new ListBean<CurrentVehicleEstimateBean>(beans, true);

    Map<Date, Record> recordsByTime = getRecordsByTimestamp(records);

    List<Date> timestamps = new ArrayList<Date>(recordsByTime.keySet());
    Collections.sort(timestamps);

    if (tryDirectMatchAgainstBlockId(query, records, recordsByTime, timestamps,
        query.getMinProbability(), beans))
      return new ListBean<CurrentVehicleEstimateBean>(beans, true);

    Set<BlockSequenceIndex> allIndices = getBlockSequenceIndicesForRecords(recordsByTime);

    for (BlockSequenceIndex index : allIndices) {

      Map<BlockInstance, List<List<BlockLocation>>> allLocations = _blockStatusService.getBlocksForIndex(
          index, timestamps);

      for (Map.Entry<BlockInstance, List<List<BlockLocation>>> entry : allLocations.entrySet()) {

        BlockInstance blockInstance = entry.getKey();
        List<List<BlockLocation>> realTimeLocations = entry.getValue();

        computeEstimatesForBlockInstance(records, recordsByTime, blockInstance,
            realTimeLocations, query.getMinProbability(), beans);
      }
    }

    Collections.sort(beans);
    return new ListBean<CurrentVehicleEstimateBean>(beans, false);
  }

  private void computeEstimatesForBlockInstance(List<Record> records,
      Map<Date, Record> recordsByTime, BlockInstance blockInstance,
      Collection<List<BlockLocation>> realTimeLocations,
      double minProbabilityForConsideration,
      List<CurrentVehicleEstimateBean> beans) {

    if (realTimeLocations.isEmpty()) {

      computeCumulativeProbabilityForScheduledBlockLocations(records,
          blockInstance, minProbabilityForConsideration, beans);

    } else {

      /**
       * Iterate over the locations, with each grouping corresponding to a
       * particular vehicle
       */
      for (List<BlockLocation> locations : realTimeLocations) {
        computeCumulativeProbabilityForRealTimeBlockLocations(recordsByTime,
            locations, minProbabilityForConsideration, beans);
      }
    }
  }

  /****
   * Private Methods
   ****/

  private boolean tryDirectMatchAgainstVehicleId(
      CurrentVehicleEstimateQueryBean query, List<Record> records,
      List<CurrentVehicleEstimateBean> beans) {

    if (query.getVehicleId() == null)
      return false;

    Record record = records.get(records.size() - 1);

    AgencyAndId vehicleId = AgencyAndIdLibrary.convertFromString(query.getVehicleId());
    BlockLocation location = _blockLocationService.getLocationForVehicleAndTime(
        vehicleId, new TargetTime(record.getTimestamp()));

    if (location == null)
      return false;

    double d = SphericalGeometryLibrary.distance(record.getLocation(),
        location.getLocation());
    double p = _realTimeLocationDeviationModel.probability(d);

    if (p < _shortCutProbability)
      return false;

    CurrentVehicleEstimateBean bean = new CurrentVehicleEstimateBean();
    bean.setProbability(p);
    bean.setTripStatus(_tripStatusBeanService.getBlockLocationAsStatusBean(
        location, query.getTime()));
    beans.add(bean);

    return true;
  }

  private boolean tryDirectMatchAgainstBlockId(
      CurrentVehicleEstimateQueryBean query, List<Record> records,
      Map<Date, Record> recordsByTime, List<Date> timestamps,
      double minProbabilityForConsideration,
      List<CurrentVehicleEstimateBean> beans) {

    String blockIdAsString = query.getBlockId();
    long serviceDate = query.getServiceDate();

    if (blockIdAsString == null || serviceDate == 0)
      return false;

    AgencyAndId blockId = AgencyAndIdLibrary.convertFromString(blockIdAsString);
    BlockInstance blockInstance = _blockCalendarService.getBlockInstance(
        blockId, serviceDate);

    if (blockInstance == null)
      return false;

    Map<AgencyAndId, List<BlockLocation>> locationsForBlockInstance = _blockLocationService.getLocationsForBlockInstance(
        blockInstance, timestamps, query.getTime());
    Collection<List<BlockLocation>> realTimeLocations = locationsForBlockInstance.values();

    computeEstimatesForBlockInstance(records, recordsByTime, blockInstance,
        realTimeLocations, minProbabilityForConsideration, beans);

    return !beans.isEmpty();
  }

  private void addResult(BlockLocation location, double cumulativeP,
      String debug, double minProbabilityForConsideration,
      List<CurrentVehicleEstimateBean> beans) {

    if (cumulativeP >= minProbabilityForConsideration) {
      CurrentVehicleEstimateBean bean = new CurrentVehicleEstimateBean();
      bean.setProbability(cumulativeP);

      TripStatusBean status = _tripStatusBeanService.getBlockLocationAsStatusBean(
          location, location.getTime());
      bean.setTripStatus(status);

      bean.setDebug(debug);

      beans.add(bean);
    }
  }

  private List<Record> getRecords(List<Record> records, long minT) {

    List<Record> pruned = new ArrayList<Record>();

    for (Record record : records) {
      if (record.getTimestamp() < minT)
        continue;

      if (record.getAccuracy() > _maxAccuracy)
        continue;

      pruned.add(record);
    }

    Collections.sort(pruned);

    return pruned;
  }

  private Map<Date, Record> getRecordsByTimestamp(List<Record> records) {

    Map<Date, Record> recordsByTime = new HashMap<Date, Record>();

    for (Record record : records) {
      Date timestamp = new Date(record.getTimestamp());
      recordsByTime.put(timestamp, record);
    }

    return recordsByTime;
  }

  private Set<BlockSequenceIndex> getBlockSequenceIndicesForRecords(
      Map<Date, Record> recordsByTime) {
    Set<BlockSequenceIndex> allIndices = null;

    for (Record record : recordsByTime.values()) {

      CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
          record.getLocation(), record.getAccuracy());

      Set<BlockSequenceIndex> indices = _blockGeospatialService.getBlockSequenceIndexPassingThroughBounds(bounds);
      if (allIndices == null)
        allIndices = indices;
      else
        allIndices.retainAll(indices);
    }
    return allIndices;
  }

  private void computeCumulativeProbabilityForRealTimeBlockLocations(
      Map<Date, Record> recordsByTime, List<BlockLocation> locations,
      double minProbabilityForConsideration,
      List<CurrentVehicleEstimateBean> beans) {

    DoubleArrayList ps = new DoubleArrayList();

    for (BlockLocation location : locations) {

      Date t = new Date(location.getTime());
      Record record = recordsByTime.get(t);

      CoordinatePoint userLocation = record.getLocation();
      CoordinatePoint vehicleLocation = location.getLocation();

      double d = SphericalGeometryLibrary.distance(userLocation,
          vehicleLocation);

      double p = _realTimeLocationDeviationModel.probability(d);
      ps.add(p);
    }

    BlockLocation last = locations.get(locations.size() - 1);
    double mu = Descriptive.mean(ps);
    String debug = asString(ps);
    addResult(last, mu, debug, minProbabilityForConsideration, beans);
  }

  private void computeCumulativeProbabilityForScheduledBlockLocations(
      List<Record> records, BlockInstance blockInstance,
      double minProbabilityForConsideration,
      List<CurrentVehicleEstimateBean> beans) {

    DoubleArrayList ps = new DoubleArrayList();
    List<ScheduledBlockLocation> blockLocations = new ArrayList<ScheduledBlockLocation>();

    Record firstRecord = records.get(0);
    ScheduledBlockLocation firstLocation = _blockGeospatialService.getBestScheduledBlockLocationForLocation(
        blockInstance, firstRecord.getLocation(), firstRecord.getTimestamp(),
        0, Double.POSITIVE_INFINITY);
    blockLocations.add(firstLocation);

    ps.add(updateScheduledBlockLocationProbability(blockInstance, firstRecord,
        firstLocation));

    Record lastRecord = records.get(records.size() - 1);
    ScheduledBlockLocation lastLocation = _blockGeospatialService.getBestScheduledBlockLocationForLocation(
        blockInstance, lastRecord.getLocation(), lastRecord.getTimestamp(), 0,
        Double.POSITIVE_INFINITY);

    ps.add(updateScheduledBlockLocationProbability(blockInstance, lastRecord,
        lastLocation));

    if (Descriptive.mean(ps) < minProbabilityForConsideration)
      return;

    /**
     * If the vehicle is traveling backwards in time, we kill the prediction
     */
    int maxTravelBackwardsTime = computeMaxTravelBackwardsTime(lastRecord.getTimestamp()
        - firstRecord.getTimestamp());

    if (lastLocation.getScheduledTime() < firstLocation.getScheduledTime()
        - maxTravelBackwardsTime)
      return;

    double minDistanceAlongBlock = Math.min(
        firstLocation.getDistanceAlongBlock(),
        lastLocation.getDistanceAlongBlock()) - 500;
    double maxDistanceAlongBlock = Math.max(
        firstLocation.getDistanceAlongBlock(),
        lastLocation.getDistanceAlongBlock()) + 500;

    for (int i = 1; i < records.size() - 1; i++) {

      Record record = records.get(i);
      ScheduledBlockLocation location = _blockGeospatialService.getBestScheduledBlockLocationForLocation(
          blockInstance, record.getLocation(), record.getTimestamp(),
          minDistanceAlongBlock, maxDistanceAlongBlock);
      blockLocations.add(location);

      ps.add(updateScheduledBlockLocationProbability(blockInstance, record,
          location));

      if (Descriptive.mean(ps) < minProbabilityForConsideration)
        return;
    }

    blockLocations.add(lastLocation);
    updateProbabilitiesWithScheduleDeviations(records, blockLocations, ps);

    BlockLocation location = _blockLocationService.getLocationForBlockInstanceAndScheduledBlockLocation(
        blockInstance, lastLocation, lastRecord.getTimestamp());
    double mu = Descriptive.mean(ps);
    String debug = asString(ps);
    addResult(location, mu, debug, minProbabilityForConsideration, beans);
  }

  private double updateScheduledBlockLocationProbability(
      BlockInstance blockInstance, Record record,
      ScheduledBlockLocation location) {

    double locationDelta = SphericalGeometryLibrary.distance(
        record.getLocation(), location.getLocation());
    double locationP = _scheduleOnlyLocationDeviationModel.probability(locationDelta);

    long serviceDate = blockInstance.getServiceDate();
    int timeDelta = (int) ((record.getTimestamp() - serviceDate) / 1000 - location.getScheduledTime());
    double scheduleP = 0.0;
    if (timeDelta < 0) {
      scheduleP = _scheduleDeviationEarlyModel.probability(-timeDelta);
    } else {
      scheduleP = _scheduleDeviationLateModel.probability(timeDelta);
    }

    return locationP * scheduleP;
  }

  private void updateProbabilitiesWithScheduleDeviations(List<Record> records,
      List<ScheduledBlockLocation> blockLocations, DoubleArrayList ps) {

    if (records.size() != blockLocations.size())
      throw new IllegalStateException();
    if (records.size() != ps.size())
      throw new IllegalStateException();

    for (int i = 1; i < records.size(); i++) {

      Record prevRecord = records.get(i - 1);
      Record nextRecord = records.get(i);
      long recordDeltaT = (nextRecord.getTimestamp() - prevRecord.getTimestamp());

      if (recordDeltaT <= 0)
        continue;

      int maxTravelBackwardsTime = computeMaxTravelBackwardsTime(recordDeltaT);

      ScheduledBlockLocation prevLocation = blockLocations.get(i - 1);
      ScheduledBlockLocation nextLocation = blockLocations.get(i);
      int locationDeltaT = nextLocation.getScheduledTime()
          - prevLocation.getScheduledTime();

      if (locationDeltaT < 0
          && Math.abs(locationDeltaT) > maxTravelBackwardsTime)
        ps.set(i, 0.0);
    }
  }

  private int computeMaxTravelBackwardsTime(long t) {
    return (int) Math.max(0, _maxTravelBackwardsTime
        - _maxTravelBackwardsDecayFactor * t / 1000);
  }

  private String asString(DoubleArrayList values) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      if (i > 0)
        b.append(',');
      b.append(_format.format(values.get(i)));
    }
    return b.toString();
  }
}
