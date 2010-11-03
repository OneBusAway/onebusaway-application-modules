package org.onebusaway.transit_data_federation.impl.otp;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.Vertex;

public abstract class AbstractBlockVertex extends AbstractVertex {

  protected final StopTimeInstance _instance;

  public AbstractBlockVertex(GraphContext context, StopTimeInstance instance) {
    super(context);
    _instance = instance;
  }

  @Override
  public String getStopId() {
    BlockStopTimeEntry bst = _instance.getStopTime();
    return bst.getStopTime().getStop().getId().toString();
  }

  @Override
  public double fastDistance(Vertex v) {

    // We can do a quick calc if the vertices are along the same block
    if (v instanceof AbstractBlockVertex) {
      AbstractBlockVertex bsv = (AbstractBlockVertex) v;
      BlockStopTimeEntry bst1 = _instance.getStopTime();
      BlockStopTimeEntry bst2 = bsv._instance.getStopTime();
      if (bst1.getTrip().getBlockConfiguration() == bst2.getTrip().getBlockConfiguration())
        return Math.abs(bst1.getDistaceAlongBlock()
            - bst2.getDistaceAlongBlock());
    }

    return super.fastDistance(v);
  }

  @Override
  public double getX() {
    BlockStopTimeEntry bst = _instance.getStopTime();
    return bst.getStopTime().getStop().getStopLon();
  }

  @Override
  public double getY() {
    BlockStopTimeEntry bst = _instance.getStopTime();
    return bst.getStopTime().getStop().getStopLat();
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
