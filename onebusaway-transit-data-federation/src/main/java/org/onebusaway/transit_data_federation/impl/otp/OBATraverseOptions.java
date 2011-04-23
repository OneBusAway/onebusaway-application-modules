package org.onebusaway.transit_data_federation.impl.otp;

import org.opentripplanner.routing.core.TraverseOptions;

public class OBATraverseOptions extends TraverseOptions {

  private static final long serialVersionUID = 1L;
  
  public long currentTime = -1;

  public boolean useRealtime = false;

  /**
   * Maximum trip duration, in milliseconds. A value of -1 indicates no upper
   * limit.
   */
  public long maxTripDuration = -1;

  public long maxInitialWaitTime = -1;
  
  public boolean extraSpecialMode = false;
}
