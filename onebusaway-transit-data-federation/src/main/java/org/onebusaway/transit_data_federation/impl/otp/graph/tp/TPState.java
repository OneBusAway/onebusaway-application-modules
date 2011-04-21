package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class TPState {

  private final TPQueryData queryData;

  private final List<Pair<StopEntry>> path;

  private final int pathIndex;

  public static TPState start(TPQueryData queryData, List<Pair<StopEntry>> path) {
    return new TPState(queryData, path, 0);
  }

  private TPState(TPQueryData queryData, List<Pair<StopEntry>> path,
      int pathIndex) {
    this.queryData = queryData;
    this.path = path;
    this.pathIndex = pathIndex;
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

  public TPState next() {
    return new TPState(queryData, path, pathIndex + 1);
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
