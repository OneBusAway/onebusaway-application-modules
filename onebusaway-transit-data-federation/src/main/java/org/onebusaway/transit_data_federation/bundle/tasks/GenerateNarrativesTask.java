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
package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.services.UniqueService;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.narrative.NarrativeProviderImpl;
import org.onebusaway.transit_data_federation.impl.narrative.NarrativeServiceImpl;
import org.onebusaway.transit_data_federation.impl.shapes.DistanceTraveledShapePointIndex;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndOrientation;
import org.onebusaway.transit_data_federation.impl.shapes.ShapePointIndex;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.modifications.Modifications;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative.Builder;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Precomputes all the link narrative objects that will power
 * {@link NarrativeService} and powered by a link {@link NarrativeProviderImpl}
 * and {@link NarrativeServiceImpl}
 * 
 * @author bdferris
 * @see NarrativeService
 * @see NarrativeServiceImpl
 * @see NarrativeProviderImpl
 */
public class GenerateNarrativesTask implements Runnable {

  private Logger _log = LoggerFactory.getLogger(GenerateNarrativesTask.class);

  @Autowired
  private FederatedTransitDataBundle _bundle;

  @Autowired
  private GtfsRelationalDao _dao;

  @Autowired
  private TransitGraphDao _graphDao;

  @Autowired
  private BlockIndexService _blockIndexService;

  @Autowired
  private Modifications _modifications;

  private ShapePointService _shapePointService;

  private UniqueService _uniqueService;

  private RefreshService _refreshService;

  private double _stopDirectionStandardDeviationThreshold = 0.7;

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Autowired
  public void setUniqueService(UniqueService uniqueService) {
    _uniqueService = uniqueService;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
  }

  public void setStopDirectionStandardDeviationThreshold(
      double stopDirectionStandardDeviationThreshold) {
    _stopDirectionStandardDeviationThreshold = stopDirectionStandardDeviationThreshold;
  }

  @Override
  public void run() {

    NarrativeProviderImpl provider = new NarrativeProviderImpl();

    generateAgencyNarratives(provider);
    generateStopNarratives(provider);
    generateNarrativesForTrips(provider);

    try {
      ObjectSerializationLibrary.writeObject(
          _bundle.getNarrativeProviderPath(), provider);
      _refreshService.refresh(RefreshableResources.NARRATIVE_DATA);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void generateAgencyNarratives(NarrativeProviderImpl provider) {

    for (Agency agency : _dao.getAllAgencies()) {

      String disclaimer = _modifications.getModificationForTypeAndId(
          AgencyNarrative.class, agency.getId(), "disclaimer");
      Boolean privateService = _modifications.getModificationForTypeAndId(
          AgencyNarrative.class, agency.getId(), "privatService");

      AgencyNarrative.Builder narrative = AgencyNarrative.builder();

      if (disclaimer != null)
        narrative.setDisclaimer(disclaimer);

      if (privateService != null)
        narrative.setPrivateService(privateService);

      provider.setNarrativeForAgency(agency.getId(), narrative.create());
    }
  }

  private void generateStopNarratives(NarrativeProviderImpl provider) {

    Map<AgencyAndId, List<ProjectedPoint>> shapePointCache = new HashMap<AgencyAndId, List<ProjectedPoint>>();
    Map<String, StopNarrative> narratives = new HashMap<String, StopNarrative>();

    Builder stopNarrativeBuilder = StopNarrative.builder();
    stopNarrativeBuilder.setDirection(null);
    StopNarrative defaultNarrative = stopNarrativeBuilder.create();

    int index = 0;

    Collection<Stop> allStops = _dao.getAllStops();
    Map<AgencyAndId, Stop> stopsById = MappingLibrary.mapToValue(allStops, "id");

    for (StopEntry stopEntry : _graphDao.getAllStops()) {

      if (index % 10 == 0)
        _log.info("stops=" + index);
      index++;

      Stop stop = stopsById.get(stopEntry.getId());

      StopNarrative narrative = defaultNarrative;

      String direction = computeStopDirection(shapePointCache, narratives,
          defaultNarrative, stop, stopEntry);

      if (direction != null) {

        narrative = narratives.get(direction);

        if (narrative == null) {
          Builder b = StopNarrative.builder();
          b.setDirection(direction);
          narrative = b.create();
          narratives.put(direction, narrative);
        }
      }

      provider.setNarrativeForStop(stopEntry.getId(), narrative);
    }

  }

  private String computeStopDirection(
      Map<AgencyAndId, List<ProjectedPoint>> shapePointCache,
      Map<String, StopNarrative> narratives, StopNarrative defaultNarrative,
      Stop stop, StopEntry stopEntry) {

    String direction = translateGtfsDirection(stop.getDirection());

    if (direction != null)
      return direction;

    Collection<PointAndOrientation> orientations = getAllOrientationsForStop(stopEntry);

    DoubleArrayList ys = new DoubleArrayList();
    DoubleArrayList xs = new DoubleArrayList();

    for (PointAndOrientation po : orientations) {
      double orientation = Math.toRadians(po.getOrientation());
      double x = Math.cos(orientation);
      double y = Math.sin(orientation);
      xs.add(x);
      ys.add(y);
    }

    if (ys.isEmpty())
      return null;

    if (ys.size() == 1) {
      double theta = Math.atan2(ys.get(0), xs.get(0));
      return getAngleAsDirection(theta);
    }

    double yMu = Descriptive.mean(ys);
    double xMu = Descriptive.mean(xs);

    /**
     * Check for undefined case where angles are directly opposite
     */
    if (yMu == 0.0 && xMu == 0.0)
      return null;

    double thetaMu = Math.atan2(yMu, xMu);

    double yVariance = Descriptive.sampleVariance(ys, yMu);
    double xVariance = Descriptive.sampleVariance(xs, xMu);

    double yStdDev = Descriptive.sampleStandardDeviation(ys.size(), yVariance);
    double xStdDev = Descriptive.sampleStandardDeviation(xs.size(), xVariance);

    if (yStdDev > _stopDirectionStandardDeviationThreshold
        || xStdDev > _stopDirectionStandardDeviationThreshold) {
      return null;
    }

    DoubleArrayList normalizedThetas = new DoubleArrayList();

    for (PointAndOrientation po : orientations) {
      double orientation = Math.toRadians(po.getOrientation());
      double delta = orientation - thetaMu;
      delta = normalizeDelta(delta);
      orientation = thetaMu + delta;
      normalizedThetas.add(orientation);
    }

    normalizedThetas.sort();
    double thetaMedian = Descriptive.median(normalizedThetas);

    return getAngleAsDirection(thetaMedian);
  }

  private double normalizeDelta(double delta) {
    while (delta < -Math.PI)
      delta += 2 * Math.PI;
    while (delta >= Math.PI)
      delta -= 2 * Math.PI;
    return delta;
  }

  private String translateGtfsDirection(String direction) {

    if (direction == null)
      return null;

    direction = direction.toLowerCase();

    if (direction.equals("north"))
      return "N";
    else if (direction.equals("east"))
      return "E";
    else if (direction.equals("south"))
      return "S";
    else if (direction.equals("west"))
      return "W";
    else if (direction.equals("northeast"))
      return "NE";
    else if (direction.equals("southeast"))
      return "SE";
    else if (direction.equals("southwest"))
      return "SW";
    else if (direction.equals("northwest"))
      return "NW";

    try {
      double orientation = Double.parseDouble(direction);
      orientation = Math.toRadians(orientation);
      return getAngleAsDirection(orientation);
    } catch (NumberFormatException ex) {

    }

    return null;
  }

  private Collection<PointAndOrientation> getAllOrientationsForStop(
      StopEntry stop) {
    List<BlockStopTimeIndex> stopTimeIndices = _blockIndexService.getStopTimeIndicesForStop(stop);

    List<PointAndOrientation> pos = new ArrayList<PointAndOrientation>();
    Map<ShapeIdAndDistance, PointAndOrientation> orientationsByKey = new HashMap<ShapeIdAndDistance, PointAndOrientation>();

    for (BlockStopTimeIndex stopTimeIndex : stopTimeIndices) {
      for (BlockStopTimeEntry blockStopTime : stopTimeIndex.getStopTimes()) {

        StopTimeEntry stopTime = blockStopTime.getStopTime();
        TripEntry trip = stopTime.getTrip();
        AgencyAndId shapeId = trip.getShapeId();

        if (shapeId == null)
          continue;

        ShapePoints shapePoints = _shapePointService.getShapePointsForShapeId(shapeId);

        if (shapePoints == null)
          continue;

        int shapePointIndex = stopTime.getShapePointIndex();

        if (shapePointIndex == -1)
          continue;

        ShapeIdAndDistance key = new ShapeIdAndDistance(shapeId,
            stopTime.getShapeDistTraveled());

        PointAndOrientation orientation = orientationsByKey.get(key);

        if (orientation == null) {

          int indexFrom = Math.max(0, shapePointIndex - 5);
          int indexTo = Math.min(shapePoints.getSize(), shapePointIndex + 5);

          ShapePointIndex shapePointIndexMethod = new DistanceTraveledShapePointIndex(
              stopTime.getShapeDistTraveled(), indexFrom, indexTo);

          orientation = shapePointIndexMethod.getPointAndOrientation(shapePoints);

          if (orientation == null)
            continue;

          orientationsByKey.put(key, orientation);
        }

        pos.add(orientation);
      }
    }

    return orientationsByKey.values();
  }

  private void generateNarrativesForTrips(NarrativeProviderImpl provider) {
    int total = 0;

    int tripIndex = 0;
    Collection<Trip> trips = _dao.getAllTrips();

    for (Trip trip : trips) {

      if (tripIndex % 200 == 0) {
        _log.info("trips=" + tripIndex + " of " + trips.size());
      }

      tripIndex++;

      TripNarrative tripNarrative = getTripNarrative(trip);
      provider.setNarrativeForTripId(trip.getId(), tripNarrative);

      List<StopTime> stopTimes = _dao.getStopTimesForTrip(trip);
      int stopTimeIndex = 0;
      for (StopTime stopTime : stopTimes) {
        StopTimeNarrative stopTimeNarrative = getStopTimeNarrative(stopTime);
        provider.setNarrativeForStopTimeEntry(trip.getId(), stopTimeIndex++,
            stopTimeNarrative);
        total++;
      }
    }
  }

  private StopTimeNarrative getStopTimeNarrative(StopTime stopTime) {
    StopTimeNarrative.Builder builder = StopTimeNarrative.builder();
    builder.setRouteShortName(deduplicate(stopTime.getRouteShortName()));
    builder.setStopHeadsign(deduplicate(stopTime.getStopHeadsign()));
    return deduplicate(builder.create());
  }

  private TripNarrative getTripNarrative(Trip trip) {

    String headsign = trip.getTripHeadsign();
    if (headsign == null) {
      Route route = trip.getRoute();
      headsign = route.getLongName();
    }

    TripNarrative.Builder builder = TripNarrative.builder();
    builder.setRouteShortName(deduplicate(trip.getRouteShortName()));
    builder.setTripHeadsign(deduplicate(headsign));
    builder.setTripShortName(deduplicate(trip.getTripShortName()));
    return builder.create();
  }

  private String getAngleAsDirection(double theta) {

    double t = Math.PI / 4;

    int r = (int) Math.floor((theta + t / 2) / t);

    switch (r) {
      case 0:
        return "E";
      case 1:
        return "NE";
      case 2:
        return "N";
      case 3:
        return "NW";
      case 4:
        return "W";
      case -1:
        return "SE";
      case -2:
        return "S";
      case -3:
        return "SW";
      case -4:
        return "W";
      default:
        return "?";
    }
  }

  private <T> T deduplicate(T object) {
    return _uniqueService.unique(object);
  }

  private static class ShapeIdAndDistance {

    private final AgencyAndId _shapeId;

    private final double _distanceAlongShape;

    public ShapeIdAndDistance(AgencyAndId shapeId, double distanceAlongShape) {
      _shapeId = shapeId;
      _distanceAlongShape = distanceAlongShape;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(_distanceAlongShape);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + ((_shapeId == null) ? 0 : _shapeId.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ShapeIdAndDistance other = (ShapeIdAndDistance) obj;
      if (Double.doubleToLongBits(_distanceAlongShape) != Double.doubleToLongBits(other._distanceAlongShape))
        return false;
      if (_shapeId == null) {
        if (other._shapeId != null)
          return false;
      } else if (!_shapeId.equals(other._shapeId))
        return false;
      return true;
    }

  }
}
