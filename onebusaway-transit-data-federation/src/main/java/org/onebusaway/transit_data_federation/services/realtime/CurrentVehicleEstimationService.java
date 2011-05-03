package org.onebusaway.transit_data_federation.services.realtime;

import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateBean;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateQueryBean;

public interface CurrentVehicleEstimationService {

  public ListBean<CurrentVehicleEstimateBean> getCurrentVehicleEstimates(
      CurrentVehicleEstimateQueryBean query);

}
