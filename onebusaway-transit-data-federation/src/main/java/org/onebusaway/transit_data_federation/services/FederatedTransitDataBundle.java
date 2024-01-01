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
package org.onebusaway.transit_data_federation.services;

import java.io.File;

/**
 * Captures the file structure of various file artifacts of a federated transit
 * data bundle. All artifact file paths are relative to a base path.
 * 
 * @author bdferris
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

  public File getTransitGraphPath() {
    return new File(_path, "TransitGraph.obj");
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

  public File getShapeGeospatialIndexDataPath() {
    return new File(_path, "ShapeGeospatialIndexData.obj.gz");
  }

  public File getServiceAlertsPath() {
    return new File(_path, "ServiceAlerts.xml");
  }

  public File getCachePath() {
    return new File(_path, "cache");
  }
  
  public File getBundleMetadataPath() {
    return new File(_path, "metadata.json");
  }

  /* NYC specific additions */
  public File getNonRevenueMoveLocationsPath() {
    return new File(_path, "NonRevenueMoveLocations.obj");
  }

  public File getNonRevenueMovePath() {
    return new File(_path, "NonRevenueMoves.obj");
  }

  public File getNonRevenueStopsPath() {
    return new File(_path, "NonRevenueStops.obj");
  }

  public File getNotInServiceDSCs() {
    return new File(_path, "NotInServiceDSCs.obj");
  }

  public File getTripsForDSCIndex() {
    return new File(_path, "TripsForDSCIndices.obj");
  }

  public File getDSCForTripIndex() {
    return new File(_path, "DSCForTripIndices.obj");
  }

  public File getBaseLocationsPath() {
    return new File(_path, "BaseLocations.txt");
  }

  public File getTerminalLocationsPath() {
    return new File(_path, "TerminalLocations.txt");
  }

  public File getTripRunDataPath() {
    return new File(_path, "TripRunData.obj");
  }
  /* end NYC specific additions */
  public File getBlockRunDataPath() {
    return new File(_path, "BlockRunData.obj");
  }

  public File getStopConsolidationFile() {
    return new File(_path, "StopConsolidation.txt");
  }

  public File getHistoricalRidershipPath() { return new File(_path, "HistoricalRiderships.obj"); }

  public File getCanonicalRoutePath() { return new File(_path, "CanonicalRoute.obj"); }

  public File getStopSwapPath() {
    return new File(_path, "WrongWayConcurrencies.obj");
  }
}
