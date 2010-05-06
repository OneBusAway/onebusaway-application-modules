package org.onebusaway.transit_data_federation.bundle.model;

import java.io.File;

public class FederatedTransitDataBundle {

  private File _path;

  public FederatedTransitDataBundle(File path) {
    _path = path;
  }

  public FederatedTransitDataBundle() {

  }

  public void setPath(File path) {
    _path = path;
  }

  public File getPath() {
    return _path;
  }

  public File getRouteSearchIndexPath() {
    return new File(_path, "RouteSearchIndex");
  }

  public File getStopSearchIndexPath() {
    return new File(_path, "StopSearchIndex");
  }

  public File getWalkPlannerGraphPath() {
    return new File(_path, "WalkPlannerGraph.obj");
  }

  public File getTripPlannerGraphPath() {
    return new File(_path, "TripPlannerGraph.obj");
  }
  
  public File getNarrativeProviderPath() {
    return new File(_path, "NarrativeProvider.obj");
  }
  
  public File getCachePath() {
    return new File(_path,"cache");
  }
}
