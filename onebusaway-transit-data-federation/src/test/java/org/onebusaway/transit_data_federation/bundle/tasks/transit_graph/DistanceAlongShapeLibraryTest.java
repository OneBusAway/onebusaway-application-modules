package org.onebusaway.transit_data_federation.bundle.tasks.transit_graph;

import static org.junit.Assert.assertEquals;

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

  @Test
  public void test01() throws IOException, DistanceAlongShapeException {

    ShapePoints shapePoints = readShapePoints("shapes.txt");
    List<StopTimeEntryImpl> stopTimes = readStopTimes("stops.txt");

    DistanceAlongShapeLibrary library = new DistanceAlongShapeLibrary();
    PointAndIndex[] points = library.getDistancesAlongShape(shapePoints, stopTimes);
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
    
    //assertEquals(18046.3, points[69].distanceAlongShape, 0.1); // STOP 29952
    assertEquals(618, points[69].index);
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
      String[] tokens = line.split(" ");
      String stopId = tokens[0];
      double lat = Double.parseDouble(tokens[1]);
      double lon = Double.parseDouble(tokens[2]);

      StopEntryImpl stop = stops.get(stopId);
      if (stop == null) {
        stop = UnitTestingSupport.stop(stopId, lat, lon);
        stops.put(stopId, stop);
      }

      StopTimeEntryImpl stopTime = UnitTestingSupport.stopTime(index, stop, trip,
          index, index, Double.NaN);
      stopTimes.add(stopTime);
    }

    reader.close();

    return stopTimes;
  }

}
