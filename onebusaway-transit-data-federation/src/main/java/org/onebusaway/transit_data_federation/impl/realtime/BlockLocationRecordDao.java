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

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Service interface for saving and querying {@link BlockLocationRecord} records
 * to and from a datastore.
 * 
 * @author bdferris
 * @see BlockLocationRecord
 */
public interface BlockLocationRecordDao {

  public void saveBlockLocationRecord(BlockLocationRecord record);

  public void saveBlockLocationRecords(List<BlockLocationRecord> records);

  /**
   * 
   * @param blockId
   * @param serviceDate the service date (Unix-time) on which the block trip operated
   * @param fromTime Unix-time
   * @param toTime Unix-time
   * @return the set of block position records with the specified trip id,
   *         service date, and time range
   */
  public List<BlockLocationRecord> getBlockLocationRecordsForBlockServiceDateAndTimeRange(
      AgencyAndId blockId, long serviceDate, long fromTime, long toTime);

  /**
   * @param vehicleId
   * @param fromTime
   * @param toTime
   * @return the set of trip position records with the specified vehicle id and
   *         time range
   */
  public List<BlockLocationRecord> getBlockLocationRecordsForVehicleAndTimeRange(
      AgencyAndId vehicleId, long fromTime, long toTime);

  /**
   * The mega-query method.  Supply what you can.
   * @param blockId
   * @param tripId
   * @param vehicleId
   * @param serviceDate
   * @param fromTime
   * @param toTime
   * @param recordLimit TODO
   * @return
   */
  public List<BlockLocationRecord> getBlockLocationRecords(AgencyAndId blockId,
      AgencyAndId tripId, AgencyAndId vehicleId, long serviceDate,
      long fromTime, long toTime, int recordLimit);
}
