package org.onebusaway.tripplanner.impl;

import edu.washington.cs.rse.collections.CollectionsLibrary;
import edu.washington.cs.rse.collections.stats.Max;
import edu.washington.cs.rse.collections.tuple.Pair;

import edu.emory.mathcs.backport.java.util.Collections;

import org.junit.Test;
import org.onebusaway.BaseTest;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.services.GtfsDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public class MaxTripVelocityExperiment extends BaseTest {

  @Autowired
  private GtfsDao _dao;

  @Test
  @Transactional
  public void go() {
    List<StopTime> stopTimes = _dao.getAllStopTimes();
    Map<String, List<StopTime>> stopTimesByTripId = CollectionsLibrary.mapToValueList(
        stopTimes, "trip.id", String.class);

    Max<Pair<StopTime>> m = new Max<Pair<StopTime>>();

    for (List<StopTime> times : stopTimesByTripId.values()) {

      Collections.sort(times);
      StopTime prev = null;
      
      Max<Pair<StopTime>> m2 = new Max<Pair<StopTime>>();

      for (StopTime stopTime : times) {

        if (prev != null) {

          int timeDiff = stopTime.getArrivalTime() - prev.getDepartureTime();
          if (timeDiff < 0) {
            System.err.println("back in time?");
            continue;
          }

          if (timeDiff == 0)
            continue;

          Stop prevStop = prev.getStop();
          Stop nextStop = stopTime.getStop();
          double distanceDiff = prevStop.getLocation().distance(
              nextStop.getLocation());

          double velocity = distanceDiff / timeDiff;
          m.add(velocity, Pair.createPair(prev, stopTime));
          m2.add(velocity, Pair.createPair(prev, stopTime));
          
        }
        prev = stopTime;
      }
      
      System.out.println(m2.getMaxValue());
    }

    double maxVelocity = m.getMaxValue();
    Pair<StopTime> pair = m.getMaxElement();
    System.out.println("vel=" + maxVelocity);
    System.out.println("  " + getDescription(pair.getFirst()));
    System.out.println("  " + getDescription(pair.getSecond()));
  }

  private String getDescription(StopTime stopTime) {
    return "route=" + stopTime.getTrip().getRoute().getShortName() + " stop="
        + stopTime.getStop().getId() + " arrival="
        + stopTime.getDepartureTime() + " departure="
        + stopTime.getArrivalTime() + " shape=" + stopTime.getShapeDistanceTraveled();
  }
}
