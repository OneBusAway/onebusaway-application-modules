package org.onebusaway.tcip.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

@XStreamAlias("piStopPointETA")
public class PiStopPointETA extends TcipMessage {
  
  private CPTSubscriptionHeader subscriptionInfo;
  
  private List<CPTStoppointIden> stoppoints;
  
  @XStreamAlias("arrival-estimates")
  private List<PISchedAdherenceCountdown> arrivalEstimates;

  public CPTSubscriptionHeader getSubscriptionInfo() {
    return subscriptionInfo;
  }

  public void setSubscriptionInfo(CPTSubscriptionHeader subscriptionInfo) {
    this.subscriptionInfo = subscriptionInfo;
  }

  public List<CPTStoppointIden> getStoppoints() {
    return stoppoints;
  }

  public void setStoppoints(List<CPTStoppointIden> stoppoints) {
    this.stoppoints = stoppoints;
  }

  public List<PISchedAdherenceCountdown> getArrivalEstimates() {
    return arrivalEstimates;
  }

  public void setArrivalEstimates(
      List<PISchedAdherenceCountdown> arrivalEstimates) {
    this.arrivalEstimates = arrivalEstimates;
  }
}
