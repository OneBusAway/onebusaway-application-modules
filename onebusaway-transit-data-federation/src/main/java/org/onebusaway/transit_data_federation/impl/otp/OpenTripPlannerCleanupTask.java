package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.springframework.beans.factory.annotation.Autowired;

public class OpenTripPlannerCleanupTask implements Runnable {

  private RefreshService _refreshService;

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  @Override
  public void run() {
    _refreshService.refresh(RefreshableResources.OTP_DATA);
  }
}
