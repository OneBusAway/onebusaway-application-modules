package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordQueryBean;

public interface VehicleStatusBeanService {

  public VehicleStatusBean getVehicleForId(AgencyAndId vehicleId, long time);

  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time);

  public ListBean<VehicleLocationRecordBean> getVehicleLocations(
      VehicleLocationRecordQueryBean query);

  public void submitVehicleLocation(VehicleLocationRecordBean record);

  public void resetVehicleLocation(AgencyAndId vehicleId);
}
