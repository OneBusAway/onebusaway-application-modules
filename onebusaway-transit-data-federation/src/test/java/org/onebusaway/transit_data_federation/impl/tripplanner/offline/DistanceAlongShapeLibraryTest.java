package org.onebusaway.transit_data_federation.impl.tripplanner.offline;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.testing.MockEntryFactory;

public class DistanceAlongShapeLibraryTest {

  @Test
  public void test01() throws IOException {

    ShapePoints shapePoints = readShapePoints("shapes.txt");
    List<StopTimeEntryImpl> stopTimes = readStopTimes("stops.txt");

    DistanceAlongShapeLibrary library = new DistanceAlongShapeLibrary();
    double[] distances = library.getDistancesAlongShape(shapePoints, stopTimes);
    assertEquals(70, distances.length);

    // Let's check a few tricky ones, including the stops that appear multiple
    // times
    assertEquals(127.6, distances[0], 0.1);
    assertEquals(3074.4, distances[10], 0.1);
    assertEquals(13759.9, distances[50], 0.1); // STOP 29952
    assertEquals(18015.3, distances[68], 0.1);
    assertEquals(18046.3, distances[69], 0.1); // STOP 29952
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

    TripEntryImpl trip = MockEntryFactory.trip("trip");
    List<StopTimeEntryImpl> stopTimes = new ArrayList<StopTimeEntryImpl>();

    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split(" ");
      String stopId = tokens[0];
      double lat = Double.parseDouble(tokens[1]);
      double lon = Double.parseDouble(tokens[2]);

      StopEntryImpl stop = stops.get(stopId);
      if (stop == null) {
        stop = MockEntryFactory.stop(stopId, lat, lon);
        stops.put(stopId, stop);
      }

      StopTimeEntryImpl stopTime = MockEntryFactory.stopTime(index, stop, trip,
          index, index, Double.NaN);
      stopTimes.add(stopTime);
    }

    reader.close();

    return stopTimes;
  }

}
