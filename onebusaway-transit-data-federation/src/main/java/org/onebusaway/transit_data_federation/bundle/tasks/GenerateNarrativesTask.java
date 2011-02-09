package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.container.refresh.RefreshService;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.GeometryLibrary;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.bundle.services.UniqueService;
import org.onebusaway.transit_data_federation.impl.ProjectedPointFactory;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.impl.narrative.NarrativeProviderImpl;
import org.onebusaway.transit_data_federation.impl.narrative.NarrativeServiceImpl;
import org.onebusaway.transit_data_federation.model.ProjectedPoint;
import org.onebusaway.transit_data_federation.model.modifications.Modifications;
import org.onebusaway.transit_data_federation.model.narrative.AgencyNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative;
import org.onebusaway.transit_data_federation.model.narrative.StopNarrative.Builder;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.model.narrative.TripNarrative;
import org.onebusaway.transit_data_federation.services.TransitDataFederationDao;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
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
  private TransitDataFederationDao _fedDao;

  @Autowired
  private Modifications _modifications;

  private UniqueService _uniqueService;

  private RefreshService _refreshService;

  @Autowired
  public void setUniqueService(UniqueService uniqueService) {
    _uniqueService = uniqueService;
  }

  @Autowired
  public void setRefreshService(RefreshService refreshService) {
    _refreshService = refreshService;
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
      AgencyNarrative.Builder narrative = AgencyNarrative.builder();
      narrative.setDisclaimer(disclaimer);
      provider.setNarrativeForAgency(agency.getId(), narrative.create());
    }
  }

  private void generateStopNarratives(NarrativeProviderImpl provider) {

    Map<AgencyAndId, List<ProjectedPoint>> shapePointCache = new HashMap<AgencyAndId, List<ProjectedPoint>>();
    Map<String, StopNarrative> narratives = new HashMap<String, StopNarrative>();

    Builder stopNarrativeBuilder = StopNarrative.builder();
    stopNarrativeBuilder.setDirection(null);
    StopNarrative defaultNarrative = stopNarrativeBuilder.create();

    Collection<Stop> stops = _dao.getAllStops();
    int index = 0;

    for (Stop stop : stops) {

      if (index % 10 == 0)
        _log.info("stops=" + index + " / " + stops.size());
      index++;

      ProjectedPoint stopP = ProjectedPointFactory.forward(stop.getLat(),
          stop.getLon());

      List<AgencyAndId> shapeIds = _fedDao.getShapeIdsForStop(stop);
      DoubleArrayList ys = new DoubleArrayList();
      DoubleArrayList xs = new DoubleArrayList();

      for (AgencyAndId shapeId : shapeIds) {
        if (shapeId != null && shapeId.hasValues()) {
          List<ProjectedPoint> shapePoints = getShapePointsForShapeId(
              shapePointCache, shapeId);
          if (shapePoints.size() < 2)
            continue;
          double orientation = getOrientationOfShapePointsNearStop(shapePoints,
              stopP);
          double x = Math.cos(orientation);
          double y = Math.sin(orientation);
          xs.add(x);
          ys.add(y);
        }
      }

      StopNarrative narrative = defaultNarrative;

      if (!ys.isEmpty()) {

        ys.sort();
        xs.sort();

        double yMedian = Descriptive.median(ys);
        double xMedian = Descriptive.median(xs);
        double thetaMedian = Math.atan2(yMedian, xMedian);
        String directionMedian = getAngleAsDirection(thetaMedian);

        // double yMu = Descriptive.mean(ys);
        // double xMu = Descriptive.mean(xs);
        // double thetaMu = Math.atan2(yMu, xMu);
        // String directionMu = getAngleAsDirection(thetaMu);

        narrative = narratives.get(directionMedian);

        if (narrative == null) {
          Builder b = StopNarrative.builder();
          b.setDirection(directionMedian);
          narrative = b.create();
          narratives.put(directionMedian, narrative);
        }

      }
      provider.setNarrativeForStop(stop.getId(), narrative);
    }
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

  private List<ProjectedPoint> getShapePointsForShapeId(
      Map<AgencyAndId, List<ProjectedPoint>> shapePointCache,
      AgencyAndId shapeId) {

    List<ProjectedPoint> points = shapePointCache.get(shapeId);

    if (points == null) {
      List<ShapePoint> shapePoints = _dao.getShapePointsForShapeId(shapeId);
      Collections.sort(shapePoints);
      points = new ArrayList<ProjectedPoint>();
      for (ShapePoint shapePoint : shapePoints)
        points.add(ProjectedPointFactory.forward(shapePoint.getLat(),
            shapePoint.getLon()));
      shapePointCache.put(shapeId, points);
    }

    return points;
  }

  private double getOrientationOfShapePointsNearStop(
      List<ProjectedPoint> shapePoints, ProjectedPoint stop) {

    XYPoint sp = new XYPoint(stop.getX(), stop.getY());

    ProjectedPoint prev = null;

    double minDistance = Double.POSITIVE_INFINITY;
    double minOrientation = Double.NaN;

    for (ProjectedPoint point : shapePoints) {

      if (prev != null) {
        if (!(prev.getLat() == point.getLat() && prev.getLon() == point.getLon())) {

          XYPoint ap = new XYPoint(prev.getX(), prev.getY());
          XYPoint bp = new XYPoint(point.getX(), point.getY());
          XYPoint seg = GeometryLibrary.projectPointToSegment(sp, ap, bp);
          double d = sp.getDistance(seg);

          if (d < minDistance) {
            minDistance = d;

            minOrientation = Math.atan2(bp.getY() - ap.getY(),
                bp.getX() - ap.getX());
          }
        }
      }
      prev = point;
    }

    return minOrientation;
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
}
