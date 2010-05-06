package org.onebusaway.tripplanner.impl;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.oba.impl.OneBusAwayWebServicePerformanceTest;
import org.onebusaway.tripplanner.model.TripPlan;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.model.TripState;
import org.onebusaway.tripplanner.services.TripPlannerService;
import org.onebusaway.tripplanner.web.common.client.model.TripPlannerConstraintsBean;
import org.onebusaway.tripplanner.web.common.client.rpc.TripPlannerWebService;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

public class TripPlannerWebServiceImplRunner {

  private static DateFormat _format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

  public static void main(String[] args) throws ParseException, ServiceException {

    String[] paths = {
        "/data-sources-common.xml", "/data-sources-server.xml", "/org/onebusaway/application-context-common.xml",
        "/org/onebusaway/application-context-server.xml", "/org/onebusaway/tripplanner/application-context-common.xml",
        "/org/onebusaway/tripplanner/application-context-server.xml"};

    ApplicationContext context = UtilityLibrary.createContext(paths);

    TripPlannerWebServiceImplRunner m = new TripPlannerWebServiceImplRunner();
    context.getAutowireCapableBeanFactory().autowireBean(m);
    m.go();
  }

  @Autowired
  private TripPlannerService _service;

  @Autowired
  private TripPlannerWebService _webService;

  @Autowired
  private GtfsDao _dao;

  @Autowired
  private ProjectionService _projection;

  public void go() throws ParseException, ServiceException {

    String start = "1/27/09 3:20 PM";
    Date startTime = _format.parse(start);

    TripPlannerConstraintsBean c = new TripPlannerConstraintsBean();
    c.setMinDepartureTime(startTime.getTime());

    TripPlannerConstraints constraints = new TripPlannerConstraints();
    constraints.setMinDepartureTime(startTime.getTime());
    constraints.setMaxTrips(3);
    constraints.setMaxTripDurationRatio(1.5);
    // constraints.setMaxComputationTime(2000);

    // 5545 36th Ave NE
    CoordinatePoint from = new CoordinatePoint(47.669799, -122.289434);

    // 3829 Burke Ave N
    CoordinatePoint to = new CoordinatePoint(47.654035, -122.335451);

    // Campus PKWY
    // CoordinatePoint to = new CoordinatePoint(47.6557188311538,
    // -122.3155760765075);

    if (false) {
      int count = 1000;
      DoubleArrayList d = new DoubleArrayList(count);
      for (int i = 0; i < count; i++) {
        long tIn = System.currentTimeMillis();
        // _webService.getTripsBetween(from.getLat(), from.getLon(),
        // to.getLat(), to.getLon(), c);
        _service.getTripsBetween(from, to, constraints);
        long tOut = System.currentTimeMillis();
        long t = tOut - tIn;
        System.out.println("i=" + i + " tDiff=" + t);
        d.add(t);
      }

      System.out.println("mu=" + Descriptive.mean(d));

    } else {
      Collection<TripPlan> trips = _service.getTripsBetween(from, to, constraints);

      TripStateDescriptions desc = new TripStateDescriptions(_dao, _projection);

      for (TripPlan trip : trips) {
        System.out.println("=== TRIP ===");
        for (TripState state : trip.getStates())
          System.out.println("  " + desc.getEncodedDescription(state));
      }
    }

  }
}
