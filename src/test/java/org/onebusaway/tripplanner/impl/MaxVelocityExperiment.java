package org.onebusaway.tripplanner.impl;

import org.onebusaway.BaseTest;
import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.tripplanner.model.StopIdsWithValues;
import org.onebusaway.tripplanner.model.TripPlannerConstants;
import org.onebusaway.tripplanner.services.StopEntry;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import com.vividsolutions.jts.geom.Point;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MaxVelocityExperiment extends BaseTest {

  @Autowired
  private TripPlannerGraph _graph;

  @Autowired
  private TripPlannerConstants _constants;

  @Autowired
  private GtfsDao _dao;

  @Test
  public void verify() {
    for (String stopId : _graph.getStopIds()) {

      StopEntry entry = _graph.getStopEntryByStopId(stopId);
      StopProxy fromStop = entry.getProxy();
      Point fromLocation = fromStop.getStopLocation();

      StopIdsWithValues times = entry.getNextStopsWithMinTimes();

      for (int i = 0; i < times.size(); i++) {

        String toStopId = times.getStopId(i);
        StopEntry toEntry = _graph.getStopEntryByStopId(toStopId);
        StopProxy toStop = toEntry.getProxy();
        Point toLocation = toStop.getStopLocation();

        double d = UtilityLibrary.distance(fromLocation, toLocation);
        int value = times.getValue(i);
        double velocity = (d / value) * (60.0 * 60.0 / 5280.0);
        if (velocity > 80 && d > 100)
          System.out.println("====== " + fromStop.getStopLat() + " " + fromStop.getStopLon() + " "
              + toStop.getStopLat() + " " + toStop.getStopLon());
      }
    }
  }
}
