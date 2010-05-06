package org.onebusaway.tcip.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

@XStreamAlias("piStopPointETASub")
public class PiStopPointETASub extends TcipMessage {
  
  private CPTSubscriptionHeader subscriptionInfo;
  
  private List<CPTStoppointIden> stoppoints;

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
}
