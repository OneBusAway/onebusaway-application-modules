package org.onebusaway.transit_data_federation.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.transit_data_federation.services.transit_graph.ServiceIdActivation;

public final class ServiceDateSummary implements
    Comparable<ServiceDateSummary>, Serializable {

  private static final long serialVersionUID = 1L;

  private final Set<ServiceIdActivation> allServiceIds;

  private final List<ServiceDate> serviceDates;

  public ServiceDateSummary(Set<ServiceIdActivation> allServiceIds,
      List<ServiceDate> dates) {
    if (allServiceIds == null)
      throw new IllegalArgumentException("allServiceIds is null");
    if (dates == null)
      throw new IllegalArgumentException("dates is null");
    this.allServiceIds = allServiceIds;
    this.serviceDates = dates;
  }

  public Set<ServiceIdActivation> getAllServiceIds() {
    return allServiceIds;
  }

  public List<ServiceDate> getDates() {
    return serviceDates;
  }

  @Override
  public int compareTo(ServiceDateSummary o) {
    return this.serviceDates.size() - o.serviceDates.size();
  }
}
