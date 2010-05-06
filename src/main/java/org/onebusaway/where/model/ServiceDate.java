/**
 * 
 */
package org.onebusaway.where.model;

import edu.washington.cs.rse.collections.stats.IntegerInterval;

import java.io.Serializable;
import java.util.Date;

public class ServiceDate implements Serializable {

  private static final long serialVersionUID = 1L;

  private String _serviceId;

  private Date _serviceDate;

  private IntegerInterval _interval;

  public ServiceDate(String serviceId, Date serviceDate,
      IntegerInterval interval) {
    _serviceId = serviceId;
    _serviceDate = serviceDate;
    _interval = interval;
  }

  public boolean hasOverlap(Date from, Date to) {
    long tFrom = _serviceDate.getTime() + _interval.getMin() * 1000;
    long tTo = _serviceDate.getTime() + _interval.getMax() * 1000;
    return from.getTime() <= tTo && tFrom <= to.getTime();
  }

  public String getServiceId() {
    return _serviceId;
  }

  public Date getServiceDate() {
    return _serviceDate;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof ServiceDate))
      return false;
    ServiceDate o = (ServiceDate) obj;
    return _serviceId.equals(o._serviceId)
        && _serviceDate.equals(o._serviceDate) && _interval.equals(o._interval);
  }

  @Override
  public int hashCode() {
    return _serviceId.hashCode() + _serviceDate.hashCode()
        + _interval.hashCode();
  }

  @Override
  public String toString() {
    return "ServiceDate(serviceId=" + _serviceId + " date=" + _serviceDate
        + ")";
  }

}