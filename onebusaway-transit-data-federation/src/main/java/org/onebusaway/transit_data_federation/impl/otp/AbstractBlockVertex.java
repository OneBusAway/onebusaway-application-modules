package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.opentripplanner.routing.core.Vertex;

public abstract class AbstractBlockVertex extends AbstractVertex {

  protected final ArrivalAndDepartureInstance _instance;

  public AbstractBlockVertex(GraphContext context,
      ArrivalAndDepartureInstance instance) {
    super(context);
    _instance = instance;
  }

  public ArrivalAndDepartureInstance getInstance() {
    return _instance;
  }

  @Override
  public String getStopId() {
    StopEntry stop = _instance.getStop();
    AgencyAndId id = stop.getId();
    return id.toString();
  }

  @Override
  public double distance(Vertex v) {

    // We can do a quick calc if the vertices are along the same block
    if (v instanceof AbstractBlockVertex) {
      AbstractBlockVertex bsv = (AbstractBlockVertex) v;
      BlockStopTimeEntry bst1 = _instance.getBlockStopTime();
      BlockStopTimeEntry bst2 = bsv._instance.getBlockStopTime();
      if (bst1.getTrip().getBlockConfiguration() == bst2.getTrip().getBlockConfiguration())
        return Math.abs(bst1.getDistanceAlongBlock()
            - bst2.getDistanceAlongBlock());
    }

    return super.distance(v);
  }

  @Override
  public double getX() {
    return _instance.getStop().getStopLon();
  }

  @Override
  public double getY() {
    return _instance.getStop().getStopLat();
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public int hashCode() {
    return _instance.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractBlockVertex bav = (AbstractBlockVertex) obj;
    return _instance.equals(bav._instance);
  }
}
