package org.onebusaway.realtime.api;

import java.util.List;

/**
 * A vehicle position listener is a component that can listen to
 * {@link VehiclePositionRecord} records incoming from an AVL data source.
 * 
 * @author bdferris
 * @see VehiclePositionRecord
 */
public interface VehiclePositionListener {
  public void handleVehiclePositionRecords(List<VehiclePositionRecord> records);
}
