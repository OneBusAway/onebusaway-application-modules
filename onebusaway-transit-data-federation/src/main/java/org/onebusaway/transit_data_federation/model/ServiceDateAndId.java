package org.onebusaway.transit_data_federation.model;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.io.Serializable;

public final class ServiceDateAndId implements Serializable {

  private static final long serialVersionUID = 1L;

  private final long serviceDate;

  private final AgencyAndId id;

  public ServiceDateAndId(long serviceDate, AgencyAndId id) {
    this.id = id;
    this.serviceDate = serviceDate;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public AgencyAndId getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (int) (serviceDate ^ (serviceDate >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof ServiceDateAndId))
      return false;
    ServiceDateAndId other = (ServiceDateAndId) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (serviceDate != other.serviceDate)
      return false;
    return true;
  }
}
