package org.onebusaway.tcip.model;

import org.onebusaway.tcip.impl.DateConverter;
import org.onebusaway.tcip.impl.DurationConverter;
import org.onebusaway.tcip.impl.TimeConverter;

import com.thoughtworks.xstream.annotations.XStreamConverter;

import java.util.Date;

public class CPTSubscriptionHeader {

  private String requestedType;

  @XStreamConverter(value = DateConverter.class)
  private Date expirationDate;

  @XStreamConverter(value = TimeConverter.class)
  private Date expirationTime;

  @XStreamConverter(value = DurationConverter.class)
  private long reportInterval;

  private int requestIdentifier;

  private int subscriberIdentifier;

  private int publisherIdentifier;

  private CPTFileApplicability applicability;

  public String getRequestedType() {
    return requestedType;
  }

  public void setRequestedType(String requestedType) {
    this.requestedType = requestedType;
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Date getExpirationTime() {
    return expirationTime;
  }

  public void setExpirationTime(Date expirationTime) {
    this.expirationTime = expirationTime;
  }

  public long getReportInterval() {
    return reportInterval;
  }

  public void setReportInterval(long reportInterval) {
    this.reportInterval = reportInterval;
  }

  public int getRequestIdentifier() {
    return requestIdentifier;
  }

  public void setRequestIdentifier(int requestIdentifier) {
    this.requestIdentifier = requestIdentifier;
  }

  public int getSubscriberIdentifier() {
    return subscriberIdentifier;
  }

  public void setSubscriberIdentifier(int subscriberIdentifier) {
    this.subscriberIdentifier = subscriberIdentifier;
  }

  public int getPublisherIdentifier() {
    return publisherIdentifier;
  }

  public void setPublisherIdentifier(int publisherIdentifier) {
    this.publisherIdentifier = publisherIdentifier;
  }

  public CPTFileApplicability getApplicability() {
    return applicability;
  }

  public void setApplicability(CPTFileApplicability applicability) {
    this.applicability = applicability;
  }

}
