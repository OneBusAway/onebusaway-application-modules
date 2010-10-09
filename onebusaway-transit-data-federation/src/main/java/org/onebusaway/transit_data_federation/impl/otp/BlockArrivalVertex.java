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

public class BlockArrivalVertex extends AbstractBlockVertex implements HasEdges {

  public BlockArrivalVertex(GraphContext graphContext, StopTimeInstance instance) {
    super(graphContext, instance);
  }

  /****
   * {@link HasEdges} Interface
   ****/

  @Override
  public int getDegreeIn() {
    return 1;
  }

  @Override
  public Collection<Edge> getIncoming() {

    BlockStopTimeEntry bst = _instance.getStopTime();
    BlockConfigurationEntry config = bst.getTrip().getBlockConfiguration();
    List<BlockStopTimeEntry> stopTimes = config.getStopTimes();

    // The assumption is that we would not have been instantiated unless we had
    // a previous stop time to arrive from.
    BlockStopTimeEntry prev = stopTimes.get(bst.getBlockSequence() - 1);
    return Arrays.asList((Edge) new BlockHopEdge(_context,
        prev, bst, _instance.getServiceDate()));
  }

  @Override
  public int getDegreeOut() {

    BlockStopTimeEntry bst = _instance.getStopTime();
    BlockConfigurationEntry config = bst.getTrip().getBlockConfiguration();
    List<BlockStopTimeEntry> stopTimes = config.getStopTimes();

    if (bst.getBlockSequence() + 1 < stopTimes.size()) {
      return 1;
    }

    return 0;
  }

  @Override
  public Collection<Edge> getOutgoing() {

    List<Edge> edges = new ArrayList<Edge>();

    BlockStopTimeEntry bst = _instance.getStopTime();
    BlockConfigurationEntry config = bst.getTrip().getBlockConfiguration();
    List<BlockStopTimeEntry> stopTimes = config.getStopTimes();

    if (bst.getBlockSequence() + 1 < stopTimes.size()) {
      edges.add(new BlockDwellEdge(_context, _instance));
    }

    return edges;
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
