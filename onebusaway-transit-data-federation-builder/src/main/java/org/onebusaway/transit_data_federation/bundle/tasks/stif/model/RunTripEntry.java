/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.bundle.tasks.stif.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;

import com.google.common.collect.ComparisonChain;

/**
 * This represents the part of a trip which is on a single run
 * 
 * @author novalis
 * 
 */
public class RunTripEntry implements Comparable<RunTripEntry> {
  private TripEntry entry;
  private String runId;
  private String runRoute;
  private String runNumber;
  private String runDepot;
  /**
   * For trips that switch runs mid way through, this is the time of the switch.
   * If this is -1, then this trip has no relief
   */
  private int reliefTime = -1;
  private ReliefState relief;
  
  static public String createId(String route, String number) {
    if (StringUtils.isEmpty(number) || StringUtils.isEmpty(route))
      return null;
    String[] tmpRun = {route, number};
    return StringUtils.join(tmpRun, "-");
  }
  
  public RunTripEntry(TripEntry entry, String runNumber, String runRoute,
      String runDepot, int reliefTime, ReliefState relief) {
    this.entry = entry;
    this.runNumber = runNumber;
    this.runRoute = runRoute;
    this.runDepot = runDepot;
    this.runId = (runDepot ==null ? RunTripEntry.createId(runRoute, runNumber) : RunTripEntry.createId(runRoute + "-" + runDepot, runNumber));
    this.reliefTime = reliefTime;
    this.relief = relief;
  }

  public int getStartTime() {
    if (getRelief() == ReliefState.AFTER_RELIEF) {
      return reliefTime;
    } else {
      // this is not really right, as a driver may get on a bus well
      // before the first stoptime (for instance if they are pulling out
      // from the depot).
      // but I don't think it will break anything.
      return entry.getStopTimes().get(0).getArrivalTime();
    }
  }

  public int getStopTime() {

    // this run could end before the last stop
    int lastTime = reliefTime;

    if (lastTime < 0) {
      List<StopTimeEntry> stopTimes = entry.getStopTimes();
      StopTimeEntry lastStopTime = stopTimes.get(stopTimes.size() - 1);
      lastTime = lastStopTime.getDepartureTime();
    }

    return lastTime;
  }

  @Override
  public int compareTo(RunTripEntry other) {
    
    if (this == other)
      return 0;
    
    int res = ComparisonChain.start()
        .compare(entry.getId(), other.getTripEntry().getId())
        .compare(runId, other.runId)
        .compare(reliefTime, other.reliefTime)
        .compare(relief, other.relief)
        .result();
    
    return res;
  }

  public TripEntry getTripEntry() {
    return entry;
  }

  public String getRunId() {
    return runId;
  }
  
  public String getRunNumber() {
    return runNumber;
  }
  
  public String getRunRoute() {
    return runRoute;
  }

  public ReliefState getRelief() {
    return relief;
  }

  public void setRelief(ReliefState relief) {
    this.relief = relief;
  }

  public String toString() {
    return "RunTripEntry(" + entry + "," + runId + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entry == null) ? 0 : entry.hashCode());
    result = prime * result + ((runId == null) ? 0 : runId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof RunTripEntry))
      return false;
    RunTripEntry other = (RunTripEntry) obj;
    if (entry == null) {
      if (other.entry != null)
        return false;
    } else if (!entry.equals(other.entry))
      return false;
    if (runId == null) {
      if (other.runId != null)
        return false;
    } else if (!runId.equals(other.runId))
      return false;
    return true;
  }
}
