package org.onebusaway.tcip.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("cptSubErrorNotice")
public class CptSubErrorNotice extends TcipMessage {

  private CPTSubscriptionHeader subscription;

  private String error;

  private String description;

  public CPTSubscriptionHeader getSubscription() {
    return subscription;
  }

  public void setSubscription(CPTSubscriptionHeader subscription) {
    this.subscription = subscription;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
