package org.onebusaway.tripplanner.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.onebusaway.common.impl.UtilityLibrary;
import org.onebusaway.tripplanner.model.StopIdsWithValues;
import org.onebusaway.tripplanner.offline.MockTripPlannerGraphFactory;
import org.onebusaway.tripplanner.offline.TripPlannerGraphImpl;
import org.onebusaway.tripplanner.services.MinTransitTimeEstimationStrategy;
import org.onebusaway.tripplanner.services.NoPathException;
import org.onebusaway.tripplanner.services.StopEntry;
import org.onebusaway.tripplanner.services.StopProxy;
import org.onebusaway.tripplanner.services.TripPlannerGraph;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMinTransitTimeEstimationStrategy {

  public abstract MinTransitTimeEstimationStrategy createStrategy(TripPlannerGraph graph, Map<String, Integer> start,
      double maxVelocity, double walkingVelocity);

  @Test
  public void go() {

    MockTripPlannerGraphFactory factory = new MockTripPlannerGraphFactory();

    factory.addStop("A", 1, 0);
    factory.addStop("B", 0, 1);
    factory.addStop("C", 1, 1);
    factory.addStop("D", 0, 2);
    factory.addStop("E", 1, 2);
    factory.addStop("F", 0, 3);
    factory.addStop("G", 0, 4);
    factory.addStop("H", 1, 4);
    factory.addStop("I", 1, 5);
    factory.addStop("J", 1, 6);

    factory.addMinTransferTime("A", "C", 100);
    factory.addMinTransferTime("B", "D", 300);
    factory.addMinTransferTime("C", "D", 500);
    factory.addMinTransferTime("C", "E", 200);
    factory.addMinTransferTime("D", "F", 400);
    factory.addMinTransferTime("E", "F", 600);
    factory.addMinTransferTime("F", "G", 200);
    factory.addMinTransferTime("F", "H", 300);
    factory.addMinTransferTime("G", "I", 300);
    factory.addMinTransferTime("H", "I", 300);
    factory.addMinTransferTime("I", "J", 200);

    TripPlannerGraphImpl graph = factory.getGraph();

    double maxVelocity = getMaxVelocity(graph);

    System.out.println("maxVelocity=" + maxVelocity);

    Map<String, Integer> initialMinTransitTimes = new HashMap<String, Integer>();
    initialMinTransitTimes.put("J", 0);

    MinTransitTimeEstimationStrategy go = createStrategy(graph, initialMinTransitTimes, maxVelocity, 1);

    try {
      int time = go.getMinTransitTime("H");
      assertEquals(500, time);
    } catch (NoPathException e) {
      fail();
    }

    try {
      int time = go.getMinTransitTime("G");
      assertEquals(500, time);
    } catch (NoPathException e) {
      fail();
    }

    try {
      int time = go.getMinTransitTime("D");
      assertEquals(1100, time);
    } catch (NoPathException e) {
      fail();
    }

    try {
      int time = go.getMinTransitTime("E");
      assertEquals(1300, time);
    } catch (NoPathException e) {
      fail();
    }

    try {
      int time = go.getMinTransitTime("A");
      assertEquals(1600, time);
    } catch (NoPathException e) {
      fail();
    }

    try {
      int time = go.getMinTransitTime("C");
      assertEquals(1500, time);
    } catch (NoPathException e) {
      fail();
    }
  }

  private double getMaxVelocity(TripPlannerGraphImpl graph) {

    double maxVelocity = 0;

    for (String fromStopId : graph.getStopIds()) {
      StopEntry fromEntry = graph.getStopEntryByStopId(fromStopId);
      StopProxy fromStop = fromEntry.getProxy();

      StopIdsWithValues entries = fromEntry.getNextStopsWithMinTimes();
      for (int i = 0; i < entries.size(); i++) {
        String toStopId = entries.getStopId(i);
        StopEntry toEntry = graph.getStopEntryByStopId(toStopId);
        StopProxy toStop = toEntry.getProxy();
        double distance = UtilityLibrary.distance(fromStop.getStopLocation(), toStop.getStopLocation());
        int time = entries.getValue(i);
        double v = distance / time;
        maxVelocity = Math.max(maxVelocity, v);
      }
    }

    return maxVelocity;
  }
}
