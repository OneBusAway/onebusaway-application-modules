package org.onebusaway.tripplanner.model;

import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.model.StopTime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StopEntry implements Serializable {

  private static final long serialVersionUID = 1L;

  private Stop stop;

  private Map<String, List<StopTime>> stopTimes;

  private Map<String,Double> transfers = new HashMap<String, Double>();

  public Stop getStop() {
    return stop;
  }

  public void setStop(Stop stop) {
    this.stop = stop;
  }

  public Map<String, List<StopTime>> getStopTimes() {
    return this.stopTimes;
  }

  public void setStopTimes(Map<String, List<StopTime>> stopTimes) {
    this.stopTimes = stopTimes;
  }

  public void addTransfer(String stop, Double distance) {
    transfers.put(stop,distance);
  }

  public Set<String> getTransfers() {
    return transfers.keySet();
  }
}
