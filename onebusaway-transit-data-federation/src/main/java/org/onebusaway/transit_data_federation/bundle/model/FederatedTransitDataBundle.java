package org.onebusaway.transit_data_federation.bundle.model;

import java.io.File;

import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreator;

/**
 * Captures the file structure of various file artifacts of a federated transit
 * data bundle. All artifact file paths are relative to a base path.
 * 
 * @author bdferris
 * @see FederatedTransitDataBundleCreator
 */
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

  public File getCalendarServiceDataPath() {
    return new File(_path, "CalendarServiceData.obj");
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

  public File getTransitGraphPath() {
    return new File(_path, "TransitGraph.obj");
  }

  public File getNarrativeProviderPath() {
    return new File(_path, "NarrativeProvider.obj");
  }

  public File getBlockTripIndicesPath() {
    return new File(_path, "BlockTripIndices.obj");
  }

  public File getFrequencyBlockTripIndicesPath() {
    return new File(_path, "FrequencyBlockTripIndices.obj");
  }

  public File getStopTransfersPath() {
    return new File(_path, "StopTransfers.obj");
  }
  
  public File getServiceAlertsPath() {
    return new File(_path, "ServiceAlerts.xml");
  }

  public File getCachePath() {
    return new File(_path, "cache");
  }
}
