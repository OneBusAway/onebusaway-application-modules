package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.HubNode;
import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferNode;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPatternService;

public class TPState {

  private final TPQueryData queryData;

  private final TransferNode node;

  private final boolean isReverse;

  public static TPState start(TPQueryData queryData, TransferNode node) {
    return new TPState(queryData, node, false);
  }

  public static TPState end(TPQueryData queryData, TransferNode node) {
    return new TPState(queryData, node, true);
  }

  private TPState(TPQueryData queryData, TransferNode node, boolean isReverse) {
    this.queryData = queryData;
    this.node = node;
    this.isReverse = isReverse;
  }

  public TPQueryData getQueryData() {
    return queryData;
  }

  public TransferNode getNode() {
    return node;
  }

  public boolean isReverse() {
    return isReverse;
  }

  public Pair<StopEntry> getStops() {
    return node.getStops();
  }

  public boolean hasTransfers() {
    return !(node.getTransfers().isEmpty() && node.getHubs().isEmpty());
  }

  public boolean isExitAllowed() {
    return node.isExitAllowed();
  }

  public List<TPState> getTransferStates(
      TransferPatternService transferPatternService) {

    List<TPState> next = new ArrayList<TPState>();

    for (TransferNode nextTransfer : node.getTransfers())
      next.add(new TPState(queryData, nextTransfer, isReverse));

    for (HubNode hubNode : node.getHubs()) {
      for (TransferNode nextTransfer : transferPatternService.expandNode(hubNode))
        next.add(new TPState(queryData, nextTransfer, isReverse));
    }

    return next;
  }

  @Override
  public String toString() {
    return node.getFromStop().getId() + " " + node.getToStop().getId();
  }
}
