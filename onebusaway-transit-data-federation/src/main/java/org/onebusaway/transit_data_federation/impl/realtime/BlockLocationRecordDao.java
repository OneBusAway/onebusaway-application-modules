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
}
