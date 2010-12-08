package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;

public interface VehicleStatusService {
  
  public VehicleLocationRecord getVehicleLocationRecordForId(AgencyAndId vehicleId);
  
  public List<VehicleLocationRecord> getAllVehicleLocationRecords();
}
