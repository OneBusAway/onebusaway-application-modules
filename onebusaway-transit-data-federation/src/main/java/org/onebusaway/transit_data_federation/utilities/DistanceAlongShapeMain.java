package org.onebusaway.transit_data_federation.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary.InvalidStopToShapeMappingException;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary.StopIsTooFarFromShapeException;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.testing.UnitTestingSupport;

public class DistanceAlongShapeMain {

  public static void main(String[] args)
      throws InvalidStopToShapeMappingException,
      StopIsTooFarFromShapeException, IOException {

    if (args.length != 2) {
      System.err.println("usage: shape.txt stops.txt");
      System.exit(-1);
    }

    DistanceAlongShapeMain m = new DistanceAlongShapeMain();
    m.run(args[0], args[1]);
  }

  public void run(String shapeFile, String stopsFile) throws IOException,
      InvalidStopToShapeMappingException, StopIsTooFarFromShapeException {

    ShapePoints shapePoints = readShapePoints(shapeFile);
    List<StopTimeEntryImpl> stopTimes = readStopTimes(stopsFile);

    DistanceAlongShapeLibrary library = new DistanceAlongShapeLibrary();
    PointAndIndex[] points = library.getDistancesAlongShape(shapePoints,
        stopTimes);
  }

  private ShapePoints readShapePoints(String path) throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader(path));

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

  private List<StopTimeEntryImpl> readStopTimes(String path) throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader(path));
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

      StopTimeEntryImpl stopTime = UnitTestingSupport.stopTime(index, stop,
          trip, index, index, Double.NaN);
      stopTimes.add(stopTime);
    }

    reader.close();

    return stopTimes;
  }

}
