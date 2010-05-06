package org.onebusaway.tripplanner.model;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import org.onebusaway.where.model.ServiceDate;

import java.util.Map;
import java.util.Set;

public class TripContext {

  private TripPlannerGraph _graph;

  private Map<String, Set<ServiceDate>> _serviceDatesByServiceId;

  private Set<ServiceDate> _serviceDates;

  private TripPlannerConstants _constants = new TripPlannerConstants();

  public void setGraph(TripPlannerGraph graph) {
    _graph = graph;
  }

  public TripPlannerGraph getGraph() {
    return _graph;
  }

  public void setServiceDates(Set<ServiceDate> serviceDates) {
    _serviceDates = serviceDates;
    _serviceDatesByServiceId = CollectionsLibrary.mapToValueSet(serviceDates,
        "serviceId", String.class);
  }

  public Set<ServiceDate> getServiceDates(TripState state) {
    return _serviceDates;
  }

  public Set<ServiceDate> getServiceDates(TripState state, String serviceId) {
    return _serviceDatesByServiceId.get(serviceId);
  }

  public TripPlannerConstants getConstants() {
    return _constants;
  }
}
