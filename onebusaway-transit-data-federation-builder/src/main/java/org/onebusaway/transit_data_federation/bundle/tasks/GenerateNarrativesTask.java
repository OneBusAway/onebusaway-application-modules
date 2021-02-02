/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
 * Copyright (C) 2015 University of South Florida (cagricetin@mail.usf.edu)
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

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
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
import org.onebusaway.transit_data_federation.model.narrative.RouteCollectionNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.blocks.BlockIndexService;
import org.onebusaway.transit_data_federation.services.blocks.BlockStopTimeIndex;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteCollectionEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.RouteEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.transit_data_federation.util.LoggingIntervalUtil;
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

  private FederatedTransitDataBundle _bundle;

  private GtfsRelationalDao _gtfsDao;

  private TransitGraphDao _transitGraphDao;

  private BlockIndexService _blockIndexService;

  private Modifications _modifications;

  private ShapePointHelper _shapePointsHelper;

  private UniqueService _uniqueService;

  private RefreshService _refreshService;

  private double _stopDirectionStandardDeviationThreshold = 0.7;
  
  
  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockIndexService(BlockIndexService blockIndexService) {
    _blockIndexService = blockIndexService;
  }

  @Autowired
  public void setModifications(Modifications modifications) {
    _modifications = modifications;
  }

  @Autowired
  public void setShapePointHelper(ShapePointHelper shapePointHelper) {
    _shapePointsHelper = shapePointHelper;
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
    generateRouteNarratives(provider);
    generateShapePointNarratives(provider);
    generateStopNarratives(provider);
    generateTripNarratives(provider);

    try {
      ObjectSerializationLibrary.writeObject(
          _bundle.getNarrativeProviderPath(), provider);
      _refreshService.refresh(RefreshableResources.NARRATIVE_DATA);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public void generateAgencyNarratives(NarrativeProviderImpl provider) {

    for (Agency agency : _gtfsDao.getAllAgencies()) {

      AgencyNarrative.Builder narrative = AgencyNarrative.builder();
      narrative.setLang(deduplicate(agency.getLang()));
      narrative.setName(deduplicate(agency.getName()));
      narrative.setPhone(deduplicate(agency.getPhone()));
      narrative.setEmail(deduplicate(agency.getEmail()));
      narrative.setTimezone(deduplicate(agency.getTimezone()));
      narrative.setUrl(deduplicate(agency.getUrl()));
      narrative.setFareUrl(agency.getFareUrl());

      String disclaimer = _modifications.getModificationForTypeAndId(
          AgencyNarrative.class, agency.getId(), "disclaimer");
      if (disclaimer != null)
        narrative.setDisclaimer(disclaimer);

      Boolean privateService = _modifications.getModificationForTypeAndId(
          AgencyNarrative.class, agency.getId(), "privatService");
      if (privateService != null)
        narrative.setPrivateService(privateService);

      provider.setNarrativeForAgency(agency.getId(), narrative.create());
    }
  }

  public void generateRouteNarratives(NarrativeProviderImpl provider) {

    for (RouteCollectionEntry routeCollectionEntry : _transitGraphDao.getAllRouteCollections()) {
      List<Route> routes = new ArrayList<Route>();
      Counter<Route> tripCounts = new Counter<Route>();
      for (RouteEntry routeEntry : routeCollectionEntry.getChildren()) {
        Route route = _gtfsDao.getRouteForId(routeEntry.getId());
        routes.add(route);
        int tripCount = routeEntry.getTrips().size();
        tripCounts.increment(route, tripCount);
      }

      RouteCollectionNarrative.Builder builder = RouteCollectionNarrative.builder();
      setPropertiesOfRouteCollectionFromRoutes(routes, tripCounts, builder);
      provider.setNarrativeForRouteCollectionId(routeCollectionEntry.getId(),
          builder.create());
    }
  }

  public void generateShapePointNarratives(NarrativeProviderImpl provider) {

    List<AgencyAndId> shapeIds = _gtfsDao.getAllShapeIds();
    int shapeSize = shapeIds.size();
    _log.info("shapes to process=" + shapeSize);
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(shapeSize) * 10;
    int index = 0;

    for (AgencyAndId shapeId : shapeIds) {
      if (index % logInterval == 0)
        _log.info("shapes=" + index);
      index++;
      ShapePoints shapePoints = _shapePointsHelper.getShapePointsForShapeId(shapeId);
      provider.setShapePointsForId(shapeId, shapePoints);
    }
  }

  public void generateStopNarratives(NarrativeProviderImpl provider) {

    Map<AgencyAndId, List<ProjectedPoint>> shapePointCache = new HashMap<AgencyAndId, List<ProjectedPoint>>();

    int index = 0;

    Collection<Stop> allStops = _gtfsDao.getAllStops();
    Map<AgencyAndId, Stop> stopsById = MappingLibrary.mapToValue(allStops, "id");
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(allStops.size());

    for (StopEntry stopEntry : _transitGraphDao.getAllStops()) {

      if (index % logInterval == 0)
        _log.info("stops=" + index);
      index++;

      Stop stop = stopsById.get(stopEntry.getId());

      StopNarrative.Builder narrative = StopNarrative.builder();
      narrative.setCode(deduplicate(stop.getCode()));
      narrative.setDescription(deduplicate(stop.getDesc()));
      narrative.setName(deduplicate(stop.getName()));
      narrative.setUrl(deduplicate(stop.getUrl()));

      String direction = computeStopDirection(provider, shapePointCache, stop,
          stopEntry);
      narrative.setDirection(deduplicate(direction));

      provider.setNarrativeForStop(stopEntry.getId(), narrative.create());
    }
  }

  public void generateTripNarratives(NarrativeProviderImpl provider) {

    int tripIndex = 0;
    Collection<Trip> trips = _gtfsDao.getAllTrips();
    int logInterval = LoggingIntervalUtil.getAppropriateLoggingInterval(trips.size());

    for (Trip trip : trips) {

      if (tripIndex % logInterval == 0) {
        _log.info("trips=" + tripIndex + " of " + trips.size());
      }

      tripIndex++;

      TripNarrative tripNarrative = getTripNarrative(trip);
      provider.setNarrativeForTripId(trip.getId(), tripNarrative);

      List<StopTime> stopTimes = _gtfsDao.getStopTimesForTrip(trip);
      int stopTimeIndex = 0;
      for (StopTime stopTime : stopTimes) {
        StopTimeNarrative stopTimeNarrative = getStopTimeNarrative(stopTime);
        provider.setNarrativeForStopTimeEntry(trip.getId(), stopTimeIndex++,
            stopTimeNarrative);
      }
    }
  }

  /****
   * Private Methods
   ****/

  private void setPropertiesOfRouteCollectionFromRoutes(List<Route> routes,
      Counter<Route> tripCounts, RouteCollectionNarrative.Builder target) {

    Counter<String> shortNames = new Counter<String>();
    Counter<String> longNames = new Counter<String>();
    Counter<String> descriptions = new Counter<String>();
    Counter<String> colors = new Counter<String>();
    Counter<String> textColors = new Counter<String>();
    Counter<String> urls = new Counter<String>();
    Counter<Integer> types = new Counter<Integer>();

    for (Route route : routes) {

      int count = tripCounts.getCount(route);

      addValueToCounterIfValid(route.getShortName(), shortNames, count);
      addValueToCounterIfValid(route.getLongName(), longNames, count);
      addValueToCounterIfValid(route.getDesc(), descriptions, count);
      addValueToCounterIfValid(route.getColor(), colors, count);
      addValueToCounterIfValid(route.getTextColor(), textColors, count);
      addValueToCounterIfValid(route.getUrl(), urls, count);

      types.increment(route.getType(), count);
    }

    if (shortNames.size() > 0)
      target.setShortName(deduplicate(shortNames.getMax()));

    if (longNames.size() > 0)
      target.setLongName(deduplicate(longNames.getMax()));

    if (descriptions.size() > 0)
      target.setDescription(deduplicate(descriptions.getMax()));

    if (colors.size() > 0)
      target.setColor(deduplicate(colors.getMax()));

    if (textColors.size() > 0)
      target.setTextColor(deduplicate(textColors.getMax()));

    if (urls.size() > 0)
      target.setUrl(deduplicate(urls.getMax()));

    target.setType(deduplicate(types.getMax()));
  }

  private <T> void addValueToCounterIfValid(String value,
      Counter<String> counts, int count) {
    value = trim(value);
    if (value != null && value.length() > 0)
      counts.increment(value, count);
  }

  private String computeStopDirection(NarrativeProviderImpl provider,
      Map<AgencyAndId, List<ProjectedPoint>> shapePointCache, Stop stop,
      StopEntry stopEntry) {

    String direction = translateGtfsDirection(stop.getDirection());

    if (direction != null)
      return direction;

    Collection<PointAndOrientation> orientations = getAllOrientationsForStop(
        provider, stopEntry);

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
      NarrativeProviderImpl provider, StopEntry stop) {
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

        ShapePoints shapePoints = provider.getShapePointsForId(shapeId);

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

  private String trim(String value) {
    if (value == null)
      return value;
    return value.trim();
  }

  private <T> T deduplicate(T object) {
    if (object == null)
      return null;
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
