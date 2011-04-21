package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import org.onebusaway.transit_data_federation.impl.otp.graph.EdgeNarrativeImpl;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.opentripplanner.routing.core.Vertex;

public class TPEdgeNarrativeImpl extends EdgeNarrativeImpl {

  private ArrivalAndDepartureInstance _departure;
  private ArrivalAndDepartureInstance _arrival;

  public TPEdgeNarrativeImpl(Vertex fromVertex, Vertex toVertex,
      ArrivalAndDepartureInstance departure, ArrivalAndDepartureInstance arrival) {
    super(fromVertex, toVertex);
    _departure = departure;
    _arrival = arrival;
  }

  public ArrivalAndDepartureInstance getDeparture() {
    return _departure;
  }

  public ArrivalAndDepartureInstance getArrival() {
    return _arrival;
  }
}
