package org.onebusaway.transit_data_federation.impl.otp;

public class OTPConfiguration {
  public boolean useRealtime = false;
  
  /**
   * Maximum trip duration, in milliseconds.  A value of -1 indicates no upper limit.
   */
  public long maxTripDuration = -1;
  
  public long maxInitialWaitTime = -1;
}
