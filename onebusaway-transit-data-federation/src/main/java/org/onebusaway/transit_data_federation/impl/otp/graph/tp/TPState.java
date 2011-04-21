package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TPState {

  private final TPQueryData queryData;

  private final List<Pair<StopEntry>> path;

  private final int pathIndex;

  private final List<Pair<ArrivalAndDepartureInstance>> instances;

  public static TPState start(TPQueryData queryData, List<Pair<StopEntry>> path) {
    List<Pair<ArrivalAndDepartureInstance>> instances = Collections.emptyList();
    return new TPState(queryData, path, 0, instances);
  }

  private TPState(TPQueryData queryData, List<Pair<StopEntry>> path,
      int pathIndex, List<Pair<ArrivalAndDepartureInstance>> instances) {
    this.queryData = queryData;
    this.path = path;
    this.pathIndex = pathIndex;
    this.instances = instances;
  }

  public TPQueryData getQueryData() {
    return queryData;
  }

  public List<Pair<StopEntry>> getPath() {
    return path;
  }

  public int getPathIndex() {
    return pathIndex;
  }

  public List<Pair<ArrivalAndDepartureInstance>> getInstances() {
    return instances;
  }

  public TPState extend(Pair<ArrivalAndDepartureInstance> pair) {
    List<Pair<ArrivalAndDepartureInstance>> extendedInstances = new ArrayList<Pair<ArrivalAndDepartureInstance>>(
        instances.size());
    extendedInstances.addAll(instances);
    extendedInstances.add(pair);
    return new TPState(queryData, path, pathIndex + 1, extendedInstances);
  }

  public boolean hasCurrentStopPair() {
    return 0 <= pathIndex && pathIndex < path.size();
  }

  public Pair<StopEntry> getCurrentStopPair() {
    return path.get(pathIndex);
  }

  public boolean hasNextStopPair() {
    return pathIndex + 1 < path.size();
  }

  public Pair<StopEntry> getNextStopPair() {
    return path.get(pathIndex + 1);
  }

  @Override
  public String toString() {
    return "path=" + path + " pathIndex=" + pathIndex;
  }
}
