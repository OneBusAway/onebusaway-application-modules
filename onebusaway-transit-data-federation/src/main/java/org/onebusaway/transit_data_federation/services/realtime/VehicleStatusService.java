package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.realtime.api.VehicleLocationRecord;

public interface VehicleStatusService {
  
  public List<VehicleLocationRecord> getAllVehicleLocationRecords();

}
