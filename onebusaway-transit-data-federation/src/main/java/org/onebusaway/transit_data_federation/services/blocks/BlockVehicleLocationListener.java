package org.onebusaway.transit_data_federation.services.blocks;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;

public interface BlockVehicleLocationListener {

  public void handleVehicleLocationRecord(VehicleLocationRecord record);

  public void resetVehicleLocation(AgencyAndId vehicleId);
}
