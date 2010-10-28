package org.onebusaway.transit_data_federation.impl.beans;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlannerConstraintsBean;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlan;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstants;
import org.onebusaway.transit_data_federation.model.tripplanner.TripPlannerConstraints;
import org.onebusaway.transit_data_federation.services.beans.TripPlanBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripPlannerBeanService;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.tripplanner.TripPlannerService;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.services.PathService;
import org.opentripplanner.routing.spt.GraphPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class TripPlannerBeanServiceImpl implements TripPlannerBeanService {

  private TripPlannerService _tripPlanner;

  private TripPlanBeanService _beanFactory;

  private TripPlannerConstants _constants;

  private PathService _pathService;

  @Autowired
  public void setTripPlannerService(TripPlannerService service) {
    _tripPlanner = service;
  }

  @Autowired
  public void setTripPlannerBeanFactory(TripPlanBeanService factory) {
    _beanFactory = factory;
  }

  @Autowired
  public void setTripPlannerConstants(TripPlannerConstants constants) {
    _constants = constants;
  }

  //@Autowired
  public void setPathService(PathService pathService) {
    _pathService = pathService;
  }

  public List<TripPlanBean> getTripsBetween(double latFrom, double lonFrom,
      double latTo, double lonTo, TripPlannerConstraintsBean constraints)
      throws ServiceException {

    if (_pathService != null) {
      String fromPlace = latFrom + "," + lonFrom;
      String toPlace = latTo + "," + lonTo;
      TraverseOptions options = new TraverseOptions();
      List<GraphPath> plans = _pathService.plan(fromPlace, toPlace,
          new Date(constraints.getMinDepartureTime()), options, 1);
      return null;
    }

    TripPlannerConstraints c = new TripPlannerConstraints();

    if (!constraints.hasMinDepartureTime())
      throw new IllegalArgumentException();

    c.setMinDepartureTime(constraints.getMinDepartureTime());

    if (constraints.hasMaxTransfers())
      c.setMaxTransferCount(constraints.getMaxTransfers());

    if (constraints.hasMaxWalkingDistance())
      c.setMaxSingleWalkDistance(constraints.getMaxWalkingDistance());

    c.setMaxTrips(3);
    c.setMaxTripDurationRatio(1.5);

    // c.setMaxComputationTime(2000);

    CoordinatePoint from = new CoordinatePoint(latFrom, lonFrom);
    CoordinatePoint to = new CoordinatePoint(latTo, lonTo);

    long t = System.currentTimeMillis();
    System.out.println("TripPlannerWebServiceImpl - A="
        + (System.currentTimeMillis() - t));
    Collection<TripPlan> plans = _tripPlanner.getTripsBetween(from, to, c);
    System.out.println("TripPlannerWebServiceImpl - B="
        + (System.currentTimeMillis() - t) + " plans=" + plans.size());
    List<TripPlanBean> beans = _beanFactory.getTripsAsBeans(plans);
    System.out.println("TripPlannerWebServiceImpl - C="
        + (System.currentTimeMillis() - t) + " beans=" + beans.size());

    return beans;
  }

  public MinTravelTimeToStopsBean getMinTravelTimeToStopsFrom(double lat,
      double lon, OneBusAwayConstraintsBean constraints)
      throws ServiceException {

    final CoordinatePoint p = new CoordinatePoint(lat, lon);

    final TripPlannerConstraints tpc = new TripPlannerConstraints();

    if (constraints.hasMinDepartureTimeOfDay()
        && constraints.hasMaxDepartureTimeOfDay()) {
      Calendar c = Calendar.getInstance();
      while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
        c.add(Calendar.DAY_OF_WEEK, 1);

      setSecondsOfDay(c, constraints.getMinDepartureTimeOfDay());
      tpc.setMinDepartureTime(c.getTimeInMillis());

      setSecondsOfDay(c, constraints.getMaxDepartureTimeOfDay());
      tpc.setMaxDepartureTime(c.getTimeInMillis());

    } else if (constraints.hasMinDepartureTime()
        && constraints.hasMaxDepartureTime()) {
      tpc.setMinDepartureTime(constraints.getMinDepartureTime());
      tpc.setMaxDepartureTime(constraints.getMaxDepartureTime());
    } else {
      throw new ServiceException("must specify departure time constraints");
    }

    long maxTripDuration = constraints.getMaxTripDuration() * 60 * 1000;
    tpc.setMaxTripDuration(maxTripDuration);

    if (constraints.hasMaxTransfers())
      tpc.setMaxTransferCount(constraints.getMaxTransfers());

    Map<StopEntry, Long> results = _tripPlanner.getMinTravelTimeToStopsFrom(p,
        tpc);
    return getStopTravelTimesAsResultsBean(results, constraints);
  }

  /****
   * Private Methods
   * 
   * @param constraints
   ****/

  private MinTravelTimeToStopsBean getStopTravelTimesAsResultsBean(
      Map<StopEntry, Long> results, OneBusAwayConstraintsBean constraints) {

    int n = results.size();

    String[] stopIds = new String[n];
    double[] lats = new double[n];
    double[] lons = new double[n];
    long[] times = new long[n];

    int index = 0;
    String agencyId = null;

    for (Map.Entry<StopEntry, Long> entry : results.entrySet()) {
      StopEntry stop = entry.getKey();
      agencyId = stop.getId().getAgencyId();
      Long time = entry.getValue();
      stopIds[index] = ApplicationBeanLibrary.getId(stop.getId());
      lats[index] = stop.getStopLat();
      lons[index] = stop.getStopLon();
      times[index] = time;
      index++;
    }
    return new MinTravelTimeToStopsBean(agencyId, stopIds, lats, lons, times,
        _constants.getWalkingVelocity());
  }

  private void setSecondsOfDay(Calendar calendar, int seconds) {
    int hours = seconds / (60 * 60);
    seconds -= hours * 60 * 60;
    int minutes = seconds % 60;
    seconds -= seconds * 60;
    calendar.set(Calendar.HOUR_OF_DAY, hours);
    calendar.set(Calendar.MINUTE, minutes);
    calendar.set(Calendar.SECOND, seconds);
    calendar.set(Calendar.MILLISECOND, 0);
  }
}
