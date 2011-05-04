package org.onebusaway.transit_data_federation.services.realtime;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;

public interface VehicleStatusService {
  
  public VehicleStatus getVehicleStatusForId(AgencyAndId vehicleId);
  
  public List<VehicleStatus> getAllVehicleStatuses();
}
