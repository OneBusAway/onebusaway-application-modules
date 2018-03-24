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

import java.io.Serializable;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onebusaway.collections.adapter.AdapterLibrary;
import org.onebusaway.collections.adapter.IAdapter;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.util.SystemTime;
import org.onebusaway.utility.EOutOfRangeStrategy;
import org.onebusaway.utility.InterpolationLibrary;

/**
 * A collection of block location records from the same block/trip/vehicle over
 * a time range, designed to maintain a cache of recent records and easily
 * interpolate location and schedule deviations from specific timestamps
 * 
 * @author bdferris
 */
public final class BlockLocationRecordCollection implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final ScheduleDeviationAdapter _scheduleDeviationAdapter = new ScheduleDeviationAdapter();

  private static final DistanceAlongBlockAdapter _distanceAlongBlockAdapter = new DistanceAlongBlockAdapter();

  private BlockInstance blockInstance;

  private AgencyAndId vehicleId;

  private final long fromTime;

  private final long toTime;

  /**
   * When we are running in simulator mode, we might be simulating a trip that
   * occurred in the past, which makes pruning records based on the currnt time
   * tricky. To deal with this, we record the measured time of the last updaate
   * according to our local clock, as opposed to whatever the record indicated.
   */
  private final long measuredLastUpdateTime;

  /**
   * Vehicle location records at particular points in time. Key = unix time ms
   */
  private final SortedMap<Long, BlockLocationRecord> records;

  public BlockLocationRecordCollection(long fromTime, long toTime,
      SortedMap<Long, BlockLocationRecord> records) {
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.records = records;
    this.measuredLastUpdateTime = SystemTime.currentTimeMillis();
  }

  public BlockLocationRecordCollection(long fromTime, long toTime) {
    this(fromTime, toTime, new TreeMap<Long, BlockLocationRecord>());

  }

  /**
   * Convenience method that creates a link
   * {@link BlockLocationRecordCollection} from a list of records
   * 
   * @param records
   * @return a collection instance from the specified records
   */
  public static BlockLocationRecordCollection createFromRecords(
      BlockInstance blockInstance, List<BlockLocationRecord> records) {

    if (records.isEmpty())
      return null;

    long fromTime = Long.MAX_VALUE;
    long toTime = Long.MIN_VALUE;
    SortedMap<Long, BlockLocationRecord> map = new TreeMap<Long, BlockLocationRecord>();

    AgencyAndId vehicleId = null;

    for (BlockLocationRecord record : records) {
      fromTime = Math.min(fromTime, record.getTime());
      toTime = Math.max(toTime, record.getTime());

      map.put(record.getTime(), record);

      vehicleId = checkVehicleId(vehicleId, record);
    }

    BlockLocationRecordCollection collection = new BlockLocationRecordCollection(
        fromTime, toTime, map);
    collection.blockInstance = blockInstance;
    collection.vehicleId = vehicleId;
    return collection;
  }

  public BlockInstance getBlockInstance() {
    return blockInstance;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public long getFromTime() {
    return fromTime;
  }

  public long getToTime() {
    return toTime;
  }

  public long getMeasuredLastUpdateTime() {
    return measuredLastUpdateTime;
  }

  public boolean isEmpty() {
    return records.isEmpty();
  }

  public double getScheduleDeviationForTargetTime(long targetTime) {

    if (records.isEmpty())
      return Double.NaN;

    SortedMap<Long, Double> m = AdapterLibrary.adaptSortedMap(records,
        _scheduleDeviationAdapter);

    return Math.round(InterpolationLibrary.interpolate(m, targetTime,
        EOutOfRangeStrategy.LAST_VALUE));
  }

  public double getDistanceAlongBlockForTargetTime(long targetTime) {

    if (records.isEmpty())
      return Double.NaN;

    SortedMap<Long, Double> m = AdapterLibrary.adaptSortedMap(records,
        _distanceAlongBlockAdapter);

    return InterpolationLibrary.interpolate(m, targetTime,
        EOutOfRangeStrategy.INTERPOLATE);
  }

  public CoordinatePoint getLastLocationForTargetTime(long targetTime) {

    BlockLocationRecord record = previousRecord(targetTime);
    if (record == null)
      return null;
    return record.getLocation();
  }

  public double getLastOrientationForTargetTime(long targetTime) {
    BlockLocationRecord record = previousRecord(targetTime);
    if (record == null)
      return Double.NaN;
    return record.getOrientation();
  }

  public EVehiclePhase getPhaseForTargetTime(long targetTime) {
    BlockLocationRecord record = previousRecord(targetTime);
    if (record == null)
      return null;
    return record.getPhase();
  }

  public String getStatusForTargetTime(long targetTime) {

    BlockLocationRecord record = previousRecord(targetTime);
    if (record == null)
      return null;
    return record.getStatus();
  }

  public long getLastUpdateTime(long targetTime) {
    if (records.isEmpty())
      return 0;
    SortedMap<Long, BlockLocationRecord> headMap = records.headMap(targetTime + 1);
    if (headMap.isEmpty())
      return records.firstKey();
    return headMap.lastKey();
  }

  public BlockLocationRecordCollection addRecord(BlockInstance blockInstance,
      BlockLocationRecord record, long windowSize) {

    AgencyAndId vehicleId = checkVehicleId(this.vehicleId, record);
    blockInstance = checkBlockInstance(this.blockInstance, blockInstance);

    long time = record.getTime();

    long updatedFromTime = Math.min(fromTime, time);
    long updatedToTime = Math.max(toTime, time);
    long updatedWindowSize = updatedToTime - updatedFromTime;

    SortedMap<Long, BlockLocationRecord> updatedRecords = new TreeMap<Long, BlockLocationRecord>(
        this.records);
    updatedRecords.put(record.getTime(), record);

    if (updatedWindowSize > windowSize) {

      double ratio = ((double) windowSize) / updatedWindowSize;
      updatedFromTime = (long) (time - (time - updatedFromTime) * ratio);
      updatedToTime = (long) (time + (updatedToTime - time) * ratio);

      updatedRecords = submap(updatedFromTime, updatedToTime, updatedRecords);
    }

    BlockLocationRecordCollection collection = new BlockLocationRecordCollection(
        updatedFromTime, updatedToTime, updatedRecords);

    collection.blockInstance = blockInstance;
    collection.vehicleId = vehicleId;

    return collection;
  }

  /****
   * Private Methods
   ****/

  private <T> SortedMap<Long, T> submap(long updatedFromTime,
      long updatedToTime, SortedMap<Long, T> map) {
    // The +1 makes sure that we included the updatedToTime in the submap
    map = map.subMap(updatedFromTime, updatedToTime + 1);
    return new TreeMap<Long, T>(map);
  }

  private BlockLocationRecord previousRecord(long targetTime) {

    if (records.isEmpty())
      return null;

    SortedMap<Long, BlockLocationRecord> headMap = records.headMap(targetTime + 1);
    if (headMap.isEmpty())
      return null;

    return headMap.get(headMap.lastKey());
  }

  private static BlockInstance checkBlockInstance(BlockInstance existing,
      BlockInstance blockInstance) {
    if (existing == null)
      return blockInstance;
    else if (!existing.equals(blockInstance)) {
      throw new IllegalArgumentException("blockInstance mismatch: expected="
          + existing + " actual=" + blockInstance);
    }
    return blockInstance;
  }

  private static AgencyAndId checkVehicleId(AgencyAndId existing,
      BlockLocationRecord record) {
    if (existing == null)
      return record.getVehicleId();
    else if (!existing.equals(record.getVehicleId()))
      throw new IllegalArgumentException("vehicleId mismatch: expected="
          + existing + " actual=" + record.getVehicleId());
    return existing;
  }

  /****
   * Static Classes
   ****/

  private static class ScheduleDeviationAdapter implements
      IAdapter<BlockLocationRecord, Double> {

    @Override
    public Double adapt(BlockLocationRecord source) {
      return source.getScheduleDeviation();
    }
  }

  private static class DistanceAlongBlockAdapter implements
      IAdapter<BlockLocationRecord, Double> {

    @Override
    public Double adapt(BlockLocationRecord source) {
      return source.getDistanceAlongBlock();
    }
  }

}
