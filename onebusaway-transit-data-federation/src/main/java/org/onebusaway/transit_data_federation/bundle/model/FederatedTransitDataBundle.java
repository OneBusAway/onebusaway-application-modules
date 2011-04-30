package org.onebusaway.transit_data_federation.bundle.model;

import java.io.File;
import java.io.FilenameFilter;

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

  private String _key;

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

  public String getKey() {
    return _key;
  }

  public void setKey(String key) {
    _key = key;
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

  public File getGraphPath() {
    return new File(_path, "Graph.obj");
  }

  public File getNarrativeProviderPath() {
    return new File(_path, "NarrativeProvider.obj");
  }

  public File getBlockTripIndicesPath() {
    return new File(_path, "BlockTripIndices.obj");
  }

  public File getBlockLayoverIndicesPath() {
    return new File(_path, "BlockLayoverIndices.obj");
  }

  public File getFrequencyBlockTripIndicesPath() {
    return new File(_path, "FrequencyBlockTripIndices.obj");
  }

  public File getStopTransfersPath() {
    return new File(_path, "StopTransfers.obj");
  }

  public File getHubStopsPath(boolean keyed) {
    return new File(_path, keyed("HubStops.txt"));
  }

  public File getTransferPatternSourceStopsPath() {
    return new File(_path, keyed("TransferPatternSourceStops.txt"));
  }

  public File getTransferPatternsPath() {
    return new File(_path, keyed("TransferPatterns"));
  }

  public File[] getAllTransferPatternsPaths() {
    File path = getTransferPatternsPath();
    if (!path.exists())
      return new File[0];
    return path.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.matches("^TransferPatterns(-\\d+){0,1}\\.gz$");
      }
    });
  }

  public File getTransferPatternsSegmentCountsPath() {
    return new File(_path, "TransferPatternsSegmentCounts.csv");
  }

  public File getSerializedTransferPatternsPath() {
    return new File(_path, "SerializedTransferPatterns.gz");
  }

  public File getServiceAlertsPath() {
    return new File(_path, "ServiceAlerts.xml");
  }

  public File getCachePath() {
    return new File(_path, "cache");
  }

  private String keyed(String value) {
    return keyed(value, true);
  }

  private String keyed(String value, boolean applyKey) {
    if (_key != null && applyKey) {
      int index = value.lastIndexOf('.');
      if (index == -1)
        return value + "-" + _key;
      else
        return value.substring(0, index) + "-" + _key + value.substring(index);
    }
    return value;
  }
}
