/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.bundle.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

  public File getShapeGeospatialIndexDataPath() {
    return new File(_path, "ShapeGeospatialIndexData.obj.gz");
  }

  public File getHubStopsPath(boolean keyed) {
    return new File(_path, keyed("HubStops.txt", keyed));
  }

  public File getTransferPatternsSourceStopsPath() {
    return new File(_path, keyed("TransferPatternsSourceStops.txt"));
  }

  public File getTransferPatternsParentPath() {
    File parent = new File(_path, "TransferPatterns");
    if (!parent.exists())
      parent.mkdirs();
    return parent;
  }

  public File getTransferPatternsPath() {
    File parent = getTransferPatternsParentPath();
    return new File(parent, keyed("TransferPatterns.gz"));
  }

  public List<File> getAllTransferPatternsPaths() {
    File path = getTransferPatternsParentPath();
    List<File> paths = new ArrayList<File>();
    getAllTransferPatternsPaths(path, paths);
    Collections.sort(paths);
    return paths;
  }

  public File getTransferPatternsTransferPointCountsPath() {
    return new File(_path, "TransferPatternsTransferPointCounts.txt");
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

  private void getAllTransferPatternsPaths(File path, List<File> paths) {
    if (path.isDirectory()) {
      for (File subPath : path.listFiles())
        getAllTransferPatternsPaths(subPath, paths);
    } else if (path.getName().matches("^TransferPatterns(-\\d+){0,1}\\.gz$")) {
      paths.add(path);
    }
  }
}
