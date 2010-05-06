package org.onebusaway.tripplanner.impl;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.common.web.common.client.rpc.ServiceException;
import org.onebusaway.tripplanner.services.StopTimeProxy;
import org.onebusaway.tripplanner.services.TripEntry;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.text.ParseException;
import java.util.List;

public class GraphTest {

  public static void main(String[] args) throws ParseException, ServiceException {

    String[] paths = {
        "/data-sources-common.xml", "/data-sources-server.xml", "/org/onebusaway/application-context-common.xml",
        "/org/onebusaway/application-context-server.xml", "/org/onebusaway/tripplanner/application-context-common.xml",
        "/org/onebusaway/tripplanner/application-context-server.xml"};

    ApplicationContext context = UtilityLibrary.createContext(paths);

    GraphTest m = new GraphTest();
    context.getAutowireCapableBeanFactory().autowireBean(m);
    m.go();
  }

  @Autowired
  TripPlannerGraph _graph;

  public void go() throws ParseException, ServiceException {
    int count = 0;
    for (String tripId : _graph.getTripIds()) {
      TripEntry entry = _graph.getTripEntryByTripId(tripId);
      List<StopTimeProxy> stopTimes = entry.getStopTimes();
      for (int i = 0; i < stopTimes.size(); i++) {
        StopTimeProxy proxy = stopTimes.get(i);
        if (proxy.getSequence() != i)
          count++;
      }
    }

    System.out.println("count=" + count);
  }
}
