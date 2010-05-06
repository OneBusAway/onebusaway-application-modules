package org.onebusaway.gtfs_diff.model;

import org.onebusaway.gtfs.model.AgencyAndId;

public class ServiceId {

  private AgencyAndId serviceId;

  public ServiceId(AgencyAndId serviceId) {
    this.serviceId = serviceId;
  }

  public AgencyAndId getServiceId() {
    return serviceId;
  }

  public void setServiceId(AgencyAndId serviceId) {
    this.serviceId = serviceId;
  }

  @Override
  public int hashCode() {
    return serviceId.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ServiceId other = (ServiceId) obj;
    return serviceId.equals(other.serviceId);
  }
}
