package org.onebusaway.transit_data_federation.impl.otp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.onebusaway.transit_data_federation.services.tripplanner.BlockConfigurationEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.StopTimeInstance;
import org.opentripplanner.routing.core.Edge;
import org.opentripplanner.routing.core.HasEdges;

public class BlockDepartureVertex extends AbstractBlockVertex implements
    HasEdges {

  public BlockDepartureVertex(GraphContext context, StopTimeInstance instance) {
    super(context, instance);
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public int getDegreeIn() {
    BlockStopTimeEntry bst = _instance.getStopTime();
    return bst.getBlockSequence() > 0 ? 1 : 0;
  }

  @Override
  public Collection<Edge> getIncoming() {

    List<Edge> edges = new ArrayList<Edge>();

    BlockStopTimeEntry bst = _instance.getStopTime();
    if (bst.getBlockSequence() > 0) {
      edges.add(new BlockDwellEdge(_context, _instance));
    }

    return edges;
  }

  @Override
  public int getDegreeOut() {
    return 1;
  }

  @Override
  public Collection<Edge> getOutgoing() {
    BlockStopTimeEntry bst = _instance.getStopTime();
    BlockConfigurationEntry config = bst.getTrip().getBlockConfiguration();
    BlockStopTimeEntry nextBlockStopTime = config.getStopTimes().get(
        bst.getBlockSequence() + 1);
    return Arrays.asList((Edge) new BlockHopEdge(_context,
        bst, nextBlockStopTime, _instance.getServiceDate()));
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || !(obj instanceof BlockArrivalVertex))
      return false;
    BlockArrivalVertex bav = (BlockArrivalVertex) obj;
    return _instance.equals(bav._instance);
  }
}
