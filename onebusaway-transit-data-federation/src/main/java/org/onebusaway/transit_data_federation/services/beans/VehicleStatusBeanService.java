package org.onebusaway.transit_data_federation.services.beans;

import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;

public interface VehicleStatusBeanService {
  public ListBean<VehicleStatusBean> getAllVehiclesForAgency(String agencyId,
      long time);
}
