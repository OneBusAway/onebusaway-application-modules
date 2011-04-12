package org.onebusaway.transit_data_federation.impl.tripplanner;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;

public class StopTransferAndHopData implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<AgencyAndId, List<StopTransferData>> transferData;

  private Map<AgencyAndId, List<StopHopData>> hopData;

  public Map<AgencyAndId, List<StopTransferData>> getTransferData() {
    return transferData;
  }

  public void setTransferData(
      Map<AgencyAndId, List<StopTransferData>> transferData) {
    this.transferData = transferData;
  }

  public Map<AgencyAndId, List<StopHopData>> getHopData() {
    return hopData;
  }

  public void setHopData(Map<AgencyAndId, List<StopHopData>> hopData) {
    this.hopData = hopData;
  }
}
