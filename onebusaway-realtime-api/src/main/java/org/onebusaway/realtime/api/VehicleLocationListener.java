package org.onebusaway.realtime.api;

import java.util.List;

/**
 * A vehicle location listener is a component that can listen to
 * {@link VehicleLocationRecord} records incoming from an AVL data source.
 * 
 * @author bdferris
 * @see VehicleLocationRecord
 */
public interface VehicleLocationListener {
  public void handleVehicleLocationRecord(VehicleLocationRecord record);

  public void handleVehicleLocationRecords(List<VehicleLocationRecord> records);
}
