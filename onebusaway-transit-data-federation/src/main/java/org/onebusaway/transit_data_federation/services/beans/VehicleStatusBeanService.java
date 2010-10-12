package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;

public interface VehicleStatusBeanService {
  
  public VehicleStatusBean getVehicleForId(AgencyAndId vehicleId, long time);
  
  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time);
}
