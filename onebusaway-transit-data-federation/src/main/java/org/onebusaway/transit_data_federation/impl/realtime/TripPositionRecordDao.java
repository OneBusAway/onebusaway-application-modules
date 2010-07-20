package org.onebusaway.transit_data_federation.impl.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

/**
 * Service interface for saving and querying {@link TripPositionRecord} records
 * to and from a datastore.
 * 
 * @author bdferris
 * @see TripPositionRecord
 */
public interface TripPositionRecordDao {

  public void saveTripPositionRecord(TripPositionRecord record);

  public void saveTripPositionRecords(List<TripPositionRecord> records);

  /**
   * 
   * @param tripId
   * @param serviceDate the service date (Unix-time) on which the trip operated
   * @param fromTime Unix-time
   * @param toTime Unix-time
   * @return the set of trip position records with the specified trip id,
   *         service date, and time range
   */
  public List<TripPositionRecord> getTripPositionRecordsForTripServiceDateAndTimeRange(
      AgencyAndId tripId, long serviceDate, long fromTime, long toTime);

  /**
   * @param vehicleId
   * @param fromTime
   * @param toTime
   * @return the set of trip position records with the specified vehicle id and
   *         time range
   */
  public List<TripPositionRecord> getTripPositionRecordsForVehicleAndTimeRange(
      AgencyAndId vehicleId, long fromTime, long toTime);

}
