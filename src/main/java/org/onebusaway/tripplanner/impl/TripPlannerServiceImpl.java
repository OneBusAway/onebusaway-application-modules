package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.services.ProjectionService;
import org.onebusaway.gtdf.model.Stop;
import org.onebusaway.gtdf.services.CalendarService;
import org.onebusaway.tripplanner.TripPlannerService;
import org.onebusaway.tripplanner.model.TripContext;
import org.onebusaway.tripplanner.model.TripPlannerConstraints;
import org.onebusaway.tripplanner.model.TripPlannerGraph;
import org.onebusaway.tripplanner.model.Trips;
import org.onebusaway.where.model.ServiceDate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class TripPlannerServiceImpl implements TripPlannerService {

  @Autowired
  private ProjectionService _projection;

  @Autowired
  private CalendarService _calendarService;

  private TripPlannerGraph _graph;

  public void setTripPlannerGraph(TripPlannerGraph graph) {
    _graph = graph;
    _graph.initialize();
  }

  public void setProjectionService(ProjectionService projection) {
    _projection = projection;
  }

  public Trips getTrips(CoordinatePoint from, CoordinatePoint to,
      long startTime, TripPlannerConstraints constraints) {

    Point pointFrom = _projection.getCoordinatePointAsPoint(from);
    Point pointTo = _projection.getCoordinatePointAsPoint(to);

    TripContext context = new TripContext();

    context.setGraph(_graph);

    Set<ServiceDate> serviceDates = _calendarService.getServiceDatesWithinRange(
        new Date(startTime), new Date(startTime + 30 * 60 * 1000));
    context.setServiceDates(serviceDates);

    PointToPointStrategy2 strategy = new PointToPointStrategy2(startTime,
        pointFrom, pointTo);

    return strategy.explore(context);
  }

  public Map<Stop, Long> getTrips(CoordinatePoint from, long startTime, long endTime) {

    Point pointFrom = _projection.getCoordinatePointAsPoint(from);

    TripContext context = new TripContext();

    context.setGraph(_graph);

    Set<ServiceDate> serviceDates = _calendarService.getServiceDatesWithinRange(
        new Date(startTime), new Date(startTime + 30 * 60 * 1000));
    context.setServiceDates(serviceDates);
    
    PointToMultipleStopsStrategy strategy = new PointToMultipleStopsStrategy(startTime,endTime,pointFrom);
    return strategy.explore(context);
  }
}
