package org.onebusaway.gtfs_diff.model;

import java.util.Date;

public class ServiceIdDateMismatch extends Mismatch {

  private ServiceId serviceIdA;

  private ServiceId serviceIdB;

  private Date dateA;

  private Date dateB;

  public ServiceIdDateMismatch(ServiceId serviceIdA, Date dateA,
      ServiceId serviceIdB, Date dateB) {
    this.serviceIdA = serviceIdA;
    this.dateA = dateA;
    this.serviceIdB = serviceIdB;
    this.dateB = dateB;
  }

  public ServiceId getServiceIdA() {
    return serviceIdA;
  }

  public void setServiceIdA(ServiceId serviceIdA) {
    this.serviceIdA = serviceIdA;
  }

  public ServiceId getServiceIdB() {
    return serviceIdB;
  }

  public void setServiceIdB(ServiceId serviceIdB) {
    this.serviceIdB = serviceIdB;
  }

  public Date getDateA() {
    return dateA;
  }

  public void setDateA(Date dateA) {
    this.dateA = dateA;
  }

  public Date getDateB() {
    return dateB;
  }

  public void setDateB(Date dateB) {
    this.dateB = dateB;
  }
}
