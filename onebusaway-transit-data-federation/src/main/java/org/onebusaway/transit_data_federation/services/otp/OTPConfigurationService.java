package org.onebusaway.transit_data_federation.services.otp;

import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data_federation.impl.otp.GraphContext;
import org.onebusaway.transit_data_federation.impl.otp.OBATraverseOptions;

public interface OTPConfigurationService {
  public GraphContext createGraphContext();
  public OBATraverseOptions createTraverseOptions();
  public void applyConstraintsToTraverseOptions(ConstraintsBean constraints,
      OBATraverseOptions options) ;
}
