package org.onebusaway.webapp.impl.oba;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.grid.GridFactory;
import org.onebusaway.geospatial.grid.TimedGridFactory;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.webapp.services.oba.OneBusAwayService;
import org.onebusaway.webapp.services.oba.OneBusAwayUserSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OneBusAwayServiceImpl implements OneBusAwayService {

  private ApplicationContext _context;

  private TransitDataService _transitDataService;

  @Autowired
  public void setApplicationContext(ApplicationContext context) {
    _context = context;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  public MinTransitTimeResult getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints, int timeSegmentSize)
      throws ServiceException {

    MinTravelTimeToStopsBean minTravelTimeToStops = _transitDataService.getMinTravelTimeToStopsFrom(
        lat, lon, constraints);

    String resultId = Long.toString(System.currentTimeMillis());
    OneBusAwayUserSession session = getSession();

    session.setOneBusAwayResults(resultId, constraints, timeSegmentSize,
        minTravelTimeToStops);

    double maxWalkDistance = constraints.getMaxWalkingDistance();
    double walkingVelocity = minTravelTimeToStops.getWalkingVelocity();

    GridFactory gridFactory = new GridFactory(5280);
    TimedGridFactory timedGridFactory = new TimedGridFactory(5280 / 4,
        walkingVelocity);

    long maxTripLength = constraints.getMaxTripDuration() * 60 * 1000;

    for (int i = 0; i < minTravelTimeToStops.getSize(); i++) {

      double stopLat = minTravelTimeToStops.getStopLat(i);
      double stopLon = minTravelTimeToStops.getStopLon(i);

      long duration = minTravelTimeToStops.getTravelTime(i);

      double remainingWalkingDistance = (maxTripLength - duration)
          * walkingVelocity;
      remainingWalkingDistance = Math.min(remainingWalkingDistance,
          maxWalkDistance);

      CoordinateBounds bounds = SphericalGeometryLibrary.bounds(stopLat,
          stopLon, remainingWalkingDistance);
      gridFactory.addBounds(bounds);

      long remainingWalkingTime = (long) (remainingWalkingDistance / walkingVelocity);
      if (remainingWalkingTime > 0)
        timedGridFactory.addPoint(stopLat, stopLon, duration,
            remainingWalkingTime);
    }

    MinTransitTimeResult result = new MinTransitTimeResult();
    result.setResultId(resultId);

    if (timeSegmentSize < 1)
      timeSegmentSize = 5;

    Map<Integer, List<EncodedPolygonBean>> polygonsByTime = timedGridFactory.getPolygonsByTime(timeSegmentSize);
    for (Map.Entry<Integer, List<EncodedPolygonBean>> entry : polygonsByTime.entrySet()) {
      int t = entry.getKey();
      for (EncodedPolygonBean bean : entry.getValue()) {
        result.getTimePolygons().add(bean);
        result.getTimes().add(t * timeSegmentSize);
      }
    }

    result.setSearchGrid(gridFactory.getGrid());
    return result;
  }

  public List<TimedPlaceBean> getLocalPaths(String resultId,
      List<LocalSearchResult> localResults) throws ServiceException {

    OneBusAwayUserSession session = getSession();

    if (!resultId.equals(session.getResultId()))
      throw new ServiceException("bad");

    OneBusAwayConstraintsBean constraints = session.getConstraints();

    MinTravelTimeToStopsBean travelTimes = session.getMinTravelTimeToStops();
    return _transitDataService.getLocalPaths(travelTimes.getAgencyId(),
        constraints, travelTimes, localResults);
  }

  public void clearCurrentResult() {
    getSession().clear();
  }

  /****
   * Private Methods
   ****/

  private OneBusAwayUserSession getSession() {
    return (OneBusAwayUserSession) _context.getBean("obaUserSession");
  }
}
