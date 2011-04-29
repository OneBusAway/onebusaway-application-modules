package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.TransferPatternData;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TPQueryData {

  /**
   * We keep one instance of {@link TransferPatternData} per transfer-pattern-based
   * trip planning request so that we can unify the transfer patterns across the
   * request
   */
  private final TransferPatternData transferPatternData = new TransferPatternData();

  private final List<StopEntry> sourceStops;

  private final List<StopEntry> destStops;

  public TPQueryData(Set<StopEntry> sourceStops, Set<StopEntry> destStops) {
    this.sourceStops = new ArrayList<StopEntry>(sourceStops);
    this.destStops = new ArrayList<StopEntry>(destStops);
    Collections.sort(this.sourceStops);
    Collections.sort(this.destStops);
  }
  
  public TransferPatternData getTransferPatternData() {
    return transferPatternData;
  }

  public List<StopEntry> getSourceStops() {
    return sourceStops;
  }

  public List<StopEntry> getDestStops() {
    return destStops;
  }
}
