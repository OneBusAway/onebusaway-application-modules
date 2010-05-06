package org.onebusaway.oba.impl;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.CalendarService;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.oba.web.common.client.rpc.OneBusAwayWebService;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;

public class OneBusAwayWebServicePerformanceTest {

  private static DateFormat _format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

  public static void main(String[] args) throws ParseException, ServiceException {

    String[] paths = {
        "/data-sources-common.xml", "/data-sources-server.xml", "/org/onebusaway/application-context-common.xml",
        "/org/onebusaway/application-context-server.xml", "/org/onebusaway/tripplanner/application-context-common.xml",
        "/org/onebusaway/tripplanner/application-context-server.xml",
        "/org/onebusaway/oba/application-context-common.xml", "/org/onebusaway/oba/application-context-server.xml"};

    ApplicationContext context = UtilityLibrary.createContext(paths);

    OneBusAwayWebServicePerformanceTest m = new OneBusAwayWebServicePerformanceTest();
    context.getAutowireCapableBeanFactory().autowireBean(m);
    m.go();
  }

  @Autowired
  private CalendarService _calendar;

  @Autowired
  private GtfsDao _dao;

  @Autowired
  private OneBusAwayWebService _service;

  public void go() throws ParseException, ServiceException {

    if (false) {
      Date date = _format.parse("2/11/09 12:00 AM");
      Set<String> serviceIds = _calendar.getServiceIdsOnDate(date);
      System.out.println("serviceIds=" + serviceIds);
      Stop stop = _dao.getStopById("578");
      List<StopTime> stopTimes = _dao.getStopTimesByStopAndServiceIds(stop, serviceIds);
      Collections.sort(stopTimes, new StopTimeComparator());

      DateFormat f = new SimpleDateFormat("hh:mm:ss aa");
      for (StopTime stopTime : stopTimes) {
        Date d = new Date(date.getTime() + stopTime.getDepartureTime() * 1000);
        System.out.println(stopTime.getTrip().getId() + " " + f.format(d));
      }
    }

    Date startTime = _format.parse("2/11/09 5:00 PM");
    Date stopTime = _format.parse("2/11/09 5:20 PM");

    OneBusAwayConstraintsBean c = new OneBusAwayConstraintsBean();

    c.setMinDepartureTime(startTime.getTime());
    c.setMaxDepartureTime(stopTime.getTime());
    c.setMaxTripDuration(30);
    c.setMaxWalkingDistance(5280 / 4);

    // 5545 36th Ave NE
    // CoordinatePoint from = new CoordinatePoint(47.669799, -122.289434);

    // 3rd and Pike
    CoordinatePoint from = new CoordinatePoint(47.610503040314235, -122.33837485313416);

    if (true) {
      int count = 200;
      DoubleArrayList d = new DoubleArrayList(count);
      for (int i = 0; i < count; i++) {
        long tIn = System.currentTimeMillis();
        _service.getMinTravelTimeToStopsFrom(from.getLat(), from.getLon(), c);
        long tOut = System.currentTimeMillis();
        long t = tOut - tIn;
        System.out.println("i=" + i + " tDiff=" + t);
        d.add(t);
      }

      System.out.println("mu=" + Descriptive.mean(d));

    } else {
      _service.getMinTravelTimeToStopsFrom(from.getLat(), from.getLon(), c);
    }
  }

  private static class StopTimeComparator implements Comparator<StopTime> {

    public int compare(StopTime o1, StopTime o2) {
      return o1.getDepartureTime() - o2.getDepartureTime();
    }
  }
}
