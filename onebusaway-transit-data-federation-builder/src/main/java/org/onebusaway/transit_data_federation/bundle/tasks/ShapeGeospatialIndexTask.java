/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ShapeGeospatialIndexTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(ShapeGeospatialIndexTask.class);
  private static double MIN_LAT_LON = -360.0;
  private static double MAX_LAT_LON = 360.0;

  private TransitGraphDao _transitGraphDao;

  private ShapePointHelper _shapePointHelper;

  private FederatedTransitDataBundle _bundle;

  private RefreshService _refreshService;

  private double _gridSize = 500;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setShapePointHelper(ShapePointHelper shapePointHelper) {
    _shapePointHelper = shapePointHelper;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  public void setGridSize(double gridSize) {
    _gridSize = gridSize;
  }

  @Override
  public void run() {
    try {
      Map<CoordinateBounds, List<AgencyAndId>> shapeIdsByGridCell = buildShapeSpatialIndex();
      File path = _bundle.getShapeGeospatialIndexDataPath();
      ObjectSerializationLibrary.writeObject(path, shapeIdsByGridCell);
      _refreshService.refresh(RefreshableResources.SHAPE_GEOSPATIAL_INDEX);
    } catch (Exception ex) {
      throw new IllegalStateException(
          "error creating shape geospatial index data", ex);
    }
  }

  /****
   * Private Methods
   ****/

  private Set<AgencyAndId> getAllShapeIds() {

    Set<AgencyAndId> shapeIds = new HashSet<AgencyAndId>();

    for (TripEntry trip : _transitGraphDao.getAllTrips()) {
      AgencyAndId shapeId = trip.getShapeId();
      if (shapeId != null)
        shapeIds.add(shapeId);
    }

    return shapeIds;
  }

  private Map<CoordinateBounds, List<AgencyAndId>> buildShapeSpatialIndex() {

    Map<CoordinatePoint, Set<AgencyAndId>> shapeIdsByGridCellCorner = new FactoryMap<CoordinatePoint, Set<AgencyAndId>>(
        new HashSet<AgencyAndId>());

    CoordinateBounds fullBounds = new CoordinateBounds();
    for (StopEntry stop : _transitGraphDao.getAllStops()) {
      if (stop.getStopLat() > MIN_LAT_LON && stop.getStopLon() > MIN_LAT_LON
          && stop.getStopLat() < MAX_LAT_LON && stop.getStopLon() < MAX_LAT_LON) {
        fullBounds.addPoint(stop.getStopLat(), stop.getStopLon());
      } else {
        _log.error("rejecting stop " + stop + " for invalid (lat,lon)=" 
            + stop.getStopLat() + ", " + stop.getStopLon());
      }
    }

    if (fullBounds.isEmpty()) {
      return Collections.emptyMap();
    }

    double centerLat = (fullBounds.getMinLat() + fullBounds.getMaxLat()) / 2;
    double centerLon = (fullBounds.getMinLon() + fullBounds.getMaxLon()) / 2;
    CoordinateBounds gridCellExample = SphericalGeometryLibrary.bounds(
        centerLat, centerLon, _gridSize / 2);

    double latStep = gridCellExample.getMaxLat() - gridCellExample.getMinLat();
    double lonStep = gridCellExample.getMaxLon() - gridCellExample.getMinLon();

    _log.info("generating shape point geospatial index...");

    Set<AgencyAndId> allShapeIds = getAllShapeIds();

    for (AgencyAndId shapeId : allShapeIds) {

      ShapePoints shapePoints = _shapePointHelper.getShapePointsForShapeId(shapeId);

      for (int i = 0; i < shapePoints.getSize(); i++) {

        double lat = shapePoints.getLatForIndex(i);
        double lon = shapePoints.getLonForIndex(i);

        addGridCellForShapePoint(shapeIdsByGridCellCorner, lat, lon, latStep,
            lonStep, shapeId);

        /**
         * If there is a particularly long stretch between shape points, we want
         * to fill in grid cells in-between
         */
        if (i > 0) {
          double prevLat = shapePoints.getLatForIndex(i - 1);
          double prevLon = shapePoints.getLonForIndex(i - 1);
          double totalDistance = SphericalGeometryLibrary.distance(prevLat,
              prevLon, lat, lon);
          for (double d = _gridSize; d < totalDistance; d += _gridSize) {
            double r = d / totalDistance;
            double latPart = (lat - prevLat) * r + prevLat;
            double lonPart = (lon - prevLon) * r + prevLon;
            addGridCellForShapePoint(shapeIdsByGridCellCorner, latPart,
                lonPart, latStep, lonStep, shapeId);
          }
        }
      }
    }

    _log.info("block shape geospatial nodes: "
        + shapeIdsByGridCellCorner.size());

    Map<CoordinateBounds, List<AgencyAndId>> shapeIdsByGridCell = new HashMap<CoordinateBounds, List<AgencyAndId>>();

    for (Map.Entry<CoordinatePoint, Set<AgencyAndId>> entry : shapeIdsByGridCellCorner.entrySet()) {
      CoordinatePoint p = entry.getKey();
      CoordinateBounds bounds = new CoordinateBounds(p.getLat(), p.getLon(),
          p.getLat() + latStep, p.getLon() + lonStep);

      List<AgencyAndId> shapeIds = new ArrayList<AgencyAndId>(entry.getValue());
      shapeIdsByGridCell.put(bounds, shapeIds);
    }

    return shapeIdsByGridCell;
  }

  private void addGridCellForShapePoint(
      Map<CoordinatePoint, Set<AgencyAndId>> shapeIdsByGridCellCorner,
      double lat, double lon, double latStep, double lonStep,
      AgencyAndId shapeId) {

    CoordinatePoint gridCellCorner = getGridCellCornerForPoint(lat, lon,
        latStep, lonStep);
    shapeIdsByGridCellCorner.get(gridCellCorner).add(shapeId);
  }

  private CoordinatePoint getGridCellCornerForPoint(double lat, double lon,
      double latStep, double lonStep) {

    double latCorner = Math.floor(lat / latStep) * latStep;
    double lonCorner = Math.floor(lon / lonStep) * lonStep;
    return new CoordinatePoint(latCorner, lonCorner);
  }

}
