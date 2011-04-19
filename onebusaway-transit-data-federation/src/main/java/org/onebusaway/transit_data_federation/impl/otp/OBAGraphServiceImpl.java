package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.opentripplanner.routing.impl.GraphServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OBAGraphServiceImpl extends GraphServiceImpl {

  public OBAGraphServiceImpl() {
    setCreateEmptyGraphIfNotFound(true);
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    setGraphPath(bundle.getGraphPath());
  }
}
