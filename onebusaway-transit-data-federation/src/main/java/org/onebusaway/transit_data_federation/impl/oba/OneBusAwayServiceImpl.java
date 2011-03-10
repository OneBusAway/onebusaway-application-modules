package org.onebusaway.transit_data_federation.impl.oba;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data_federation.impl.beans.ApplicationBeanLibrary;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.WalkPlannerConstraints;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.oba.OneBusAwayService;
import org.onebusaway.transit_data_federation.services.walkplanner.NoPathException;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerService;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
class OneBusAwayServiceImpl implements OneBusAwayService {

  @Autowired
  private TripPlannerConstants _constants;

  public WalkPlannerService _walkPlanner;

  @Autowired
  public void setWalkPlannerService(WalkPlannerService walkPlanner) {
    _walkPlanner = walkPlanner;
  }

  public List<TimedPlaceBean> getLocalPaths(
      OneBusAwayConstraintsBean constraints,
      MinTravelTimeToStopsBean travelTimes, List<LocalSearchResult> localResults)
      throws ServiceException {

    long maxTripLength = constraints.getMaxTripDuration() * 60 * 1000;

    List<TimedPlaceBean> beans = new ArrayList<TimedPlaceBean>();

    double walkingVelocity = travelTimes.getWalkingVelocity();

    for (LocalSearchResult result : localResults) {

      double placeLat = result.getLat();
      double placeLon = result.getLon();

      List<TripToStop> closestStops = new ArrayList<TripToStop>();

      for (int index = 0; index < travelTimes.getSize(); index++) {

        String stopIdAsString = travelTimes.getStopId(index);
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(stopIdAsString);

        long currentTripDuration = travelTimes.getTravelTime(index);
        double stopLat = travelTimes.getStopLat(index);
        double stopLon = travelTimes.getStopLon(index);
        double d = SphericalGeometryLibrary.distance(stopLat, stopLon,
            placeLat, placeLon);
        double t = currentTripDuration + d / walkingVelocity;
        if (d <= constraints.getMaxWalkingDistance() && t < maxTripLength) {
          closestStops.add(new TripToStop(stopId, currentTripDuration, t, index));
        }
      }

      if (closestStops.isEmpty())
        continue;

      Collections.sort(closestStops);

      double minTime = 0;
      TripToStop minStop = null;

      CoordinatePoint place = new CoordinatePoint(result.getLat(),
          result.getLon());

      for (TripToStop o : closestStops) {

        long currentTripDuration = o.getTransitTimeToStop();
        double minTimeToPlace = o.getMinTansitTimeToPlace();

        // Short circuit if there is no way any of the remaining trips is going
        // to be better than our current winner
        if (minStop != null && minTimeToPlace > minTime)
          break;

        try {

          WalkPlannerConstraints c = new WalkPlannerConstraints();
          c.setMaxTripLength(maxTripLength - currentTripDuration);

          int index = o.getIndex();
          double stopLat = travelTimes.getStopLat(index);
          double stopLon = travelTimes.getStopLon(index);
          CoordinatePoint stopLocation = new CoordinatePoint(stopLat, stopLon);
          WalkPlan plan = _walkPlanner.getWalkPlan(stopLocation, place, c);

          double t = currentTripDuration + plan.getDistance()
              / _constants.getWalkingVelocity();
          if (minStop == null || t < minTime) {
            minTime = t;
            minStop = o;
          }
        } catch (NoPathException e) {
        }
      }

      if (minStop != null && minTime <= maxTripLength) {
        TimedPlaceBean bean = new TimedPlaceBean();
        bean.setPlaceId(result.getId());
        bean.setStopId(ApplicationBeanLibrary.getId(minStop.getStopId()));
        bean.setTime((int) (minTime / 1000));
        beans.add(bean);
      }
    }

    return beans;
  }

  /****
   * Private Methods
   ****/

  private static class TripToStop implements Comparable<TripToStop> {

    private AgencyAndId _stopId;
    private long _transitTimeToStop;
    private double _minTransitTimeToPlace;
    private int _index;

    public TripToStop(AgencyAndId stopId, long transitTimeToStop,
        double minTransitTimeToPlace, int index) {
      _stopId = stopId;
      _transitTimeToStop = transitTimeToStop;
      _minTransitTimeToPlace = minTransitTimeToPlace;
      _index = index;
    }

    public AgencyAndId getStopId() {
      return _stopId;
    }

    public long getTransitTimeToStop() {
      return _transitTimeToStop;
    }

    public double getMinTansitTimeToPlace() {
      return _minTransitTimeToPlace;
    }

    public int getIndex() {
      return _index;
    }

    public int compareTo(TripToStop o) {
      return _minTransitTimeToPlace == o._minTransitTimeToPlace ? 0
          : (_minTransitTimeToPlace < o._minTransitTimeToPlace ? -1 : 1);
    }
  }
}
