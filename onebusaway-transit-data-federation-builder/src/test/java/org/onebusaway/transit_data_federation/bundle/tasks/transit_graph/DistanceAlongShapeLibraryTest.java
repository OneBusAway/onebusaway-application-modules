/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary.DistanceAlongShapeException;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.testing.UnitTestingSupport;

public class DistanceAlongShapeLibraryTest {

  private final double STOP_TO_SHAPE_DISTANCE = 0.01;
  @Test
  public void test01() throws IOException, DistanceAlongShapeException {

    ShapePoints shapePoints = readShapePoints("shapes-01.txt");
    List<StopTimeEntryImpl> stopTimes = readStopTimes("stops-01.txt");

    DistanceAlongShapeLibrary library = new DistanceAlongShapeLibrary();
    PointAndIndex[] points = library.getDistancesAlongShape(shapePoints,
        stopTimes);
    assertEquals(70, points.length);

    // Let's check a few tricky ones, including the stops that appear multiple
    // times
    assertEquals(127.6, points[0].distanceAlongShape, 0.1); // STOP 1610
    assertEquals(2, points[0].index);

    assertEquals(3074.4, points[10].distanceAlongShape, 0.1); // STOP 9390
    assertEquals(95, points[10].index);

    assertEquals(13759.9, points[50].distanceAlongShape, 0.1); // STOP 29952
    assertEquals(452, points[50].index);

    assertEquals(18015.3, points[68].distanceAlongShape, 0.1); // STOP 29090
    assertEquals(616, points[68].index);

    // assertEquals(18046.3, points[69].distanceAlongShape, 0.1); // STOP 29952
    assertEquals(618, points[69].index);
  }

  @Test
  public void test02() throws IOException, DistanceAlongShapeException {
    ShapePoints shapePoints = readShapePoints("shapes-02.txt");
    List<StopTimeEntryImpl> stopTimes = readStopTimes("stops-02.txt");

    DistanceAlongShapeLibrary library = new DistanceAlongShapeLibrary();
    PointAndIndex[] points = library.getDistancesAlongShape(shapePoints,
        stopTimes);
    assertEquals(4, points.length);

    assertEquals(155.4, points[0].distanceAlongShape, 0.1); // STOP A
    assertEquals(1, points[0].index);

    assertEquals(816.7, points[1].distanceAlongShape, 0.1); // STOP B
    assertEquals(4, points[1].index);

    assertEquals(819.3, points[2].distanceAlongShape, 0.1); // STOP C
    assertEquals(5, points[2].index);

    assertEquals(1151.1, points[3].distanceAlongShape, 0.1); // STOP D
    assertEquals(9, points[3].index);
  }

  @Test
  public void test03() throws IOException, DistanceAlongShapeException {

    ShapePoints shapePoints = readShapePoints("shapes-03.txt");
    List<StopTimeEntryImpl> stopTimes = readStopTimes("stops-03.txt");

    DistanceAlongShapeLibrary library = new DistanceAlongShapeLibrary();
    PointAndIndex[] points = library.getDistancesAlongShape(shapePoints,
        stopTimes);
    assertEquals(5, points.length);

    assertEquals(67.9, points[0].distanceAlongShape, 0.1); // STOP A
    assertEquals(0, points[0].index);

    assertEquals(446.0, points[1].distanceAlongShape, 0.1); // STOP B
    assertEquals(1, points[1].index);

    assertEquals(852.7, points[2].distanceAlongShape, 0.1); // STOP C
    assertEquals(2, points[2].index);

    assertEquals(1247.0, points[3].distanceAlongShape, 0.1); // STOP D
    assertEquals(3, points[3].index);

  }

  /**
    * WMATAs Route H6 is a fun example of complexity.  It contains
    * 3 different stopping patterns;
    * A loop;
    * A figure 8.
    * Verify the snapping algorithm does the right thing with it.
    * TODO: ideally this test would verify stops and shapes.  For now,
    *  it simply verifies the stop ordering via the distance along shape.
   */
  @Test
  public void testWmataH6() throws IOException, DistanceAlongShapeException {
    ShapePoints shapePoints = readShapePoints("shapes-h6.txt");
    List<StopTimeEntryImpl> stopTimes = readStopTimes("stops-h6.txt");
    DistanceAlongShapeLibrary library = new DistanceAlongShapeLibrary();
    PointAndIndex[] points = library.getDistancesAlongShape(shapePoints,
            stopTimes);
    assertEquals(25, points.length);
    double lastDAB = matchStopToPoint(stopTimes, points, 0, "7349", 0.0);
    lastDAB = matchStopToPoint(stopTimes, points, 1, "7233", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 2, "7253", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 3, "7257", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 4, "16967", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 5, "7130", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 6, "19037", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 7, "19038", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 8, "7078", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 9, "7133", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 10, "7203", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 11, "7233", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 12, "7253", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 13, "7257", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 14, "7291", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 15, "7420", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 16, "7476", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 17, "17248", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 18, "16934", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 19, "17249", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 20, "7594", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 21, "18884", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 22, "7452", lastDAB);
    lastDAB = matchStopToPoint(stopTimes, points, 23, "7435", lastDAB);
    matchStopToPoint(stopTimes, points, 24, "7321", lastDAB);

  }

  private double matchStopToPoint(List<StopTimeEntryImpl> stopTimes, PointAndIndex[] points, int i, String stopId, double distanceAlongBlock) {
    StopEntryImpl expectedStop = null;
    for (StopTimeEntryImpl stei : stopTimes) {
      if (stei.getStop().getId().getId().equals(stopId)) {
        expectedStop = stei.getStop();
      }
    }
    if (expectedStop == null)
      throw new IllegalArgumentException("stop " + stopId + " not found in " + stopTimes);
    assertTrue(distanceAlongBlock < points[i].distanceAlongShape);
    return points[i].distanceAlongShape;


  }

  private ShapePoints readShapePoints(String key) throws IOException {

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream(
            "DistancesAlongShapeLibraryTest-" + key)));
    String line = null;
    ShapePointsFactory factory = new ShapePointsFactory();

    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split(" ");
      double lat = Double.parseDouble(tokens[0]);
      double lon = Double.parseDouble(tokens[1]);
      factory.addPoint(lat, lon);
    }

    reader.close();

    return factory.create();
  }

  private List<StopTimeEntryImpl> readStopTimes(String key) throws IOException {

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream(
            "DistancesAlongShapeLibraryTest-" + key)));
    String line = null;

    Map<String, StopEntryImpl> stops = new HashMap<String, StopEntryImpl>();

    int index = 0;

    TripEntryImpl trip = UnitTestingSupport.trip("trip");
    List<StopTimeEntryImpl> stopTimes = new ArrayList<StopTimeEntryImpl>();

    while ((line = reader.readLine()) != null) {
      try {
        String[] tokens = line.split(" ");
        String stopId = tokens[0];
        double lat = Double.parseDouble(tokens[1]);
        double lon = Double.parseDouble(tokens[2]);

        StopEntryImpl stop = stops.get(stopId);
        if (stop == null) {
          stop = UnitTestingSupport.stop(stopId, lat, lon);
          stops.put(stopId, stop);
        }

        StopTimeEntryImpl stopTime = UnitTestingSupport.stopTime(index, stop,
                trip, index, index, Double.NaN);
        stopTimes.add(stopTime);
      } catch (ArrayIndexOutOfBoundsException a) {
        System.err.println("invalid line |" + line + "| in file" + "DistancesAlongShapeLibraryTest-" + key);
        throw a;
      }
    }

    reader.close();

    return stopTimes;
  }

}
