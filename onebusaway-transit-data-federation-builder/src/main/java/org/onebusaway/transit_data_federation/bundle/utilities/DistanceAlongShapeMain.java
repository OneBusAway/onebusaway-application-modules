/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.bundle.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary;
import org.onebusaway.transit_data_federation.bundle.tasks.transit_graph.DistanceAlongShapeLibrary.DistanceAlongShapeException;
import org.onebusaway.transit_data_federation.impl.shapes.PointAndIndex;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.StopTimeEntryImpl;
import org.onebusaway.transit_data_federation.impl.transit_graph.TripEntryImpl;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.ShapePointsFactory;
import org.onebusaway.transit_data_federation.testing.UnitTestingSupport;

public class DistanceAlongShapeMain {

  public static void main(String[] args)
 throws IOException,
      DistanceAlongShapeException {

    if (args.length != 2) {
      System.err.println("usage: shape.txt stops.txt");
      System.exit(-1);
    }

    DistanceAlongShapeMain m = new DistanceAlongShapeMain();
    m.run(args[0], args[1]);
  }

  public void run(String shapeFile, String stopsFile) throws IOException,
      DistanceAlongShapeException {

    ShapePoints shapePoints = readShapePoints(shapeFile);
    List<StopTimeEntryImpl> stopTimes = readStopTimes(stopsFile);

    DistanceAlongShapeLibrary library = new DistanceAlongShapeLibrary();
    PointAndIndex[] points = library.getDistancesAlongShape(shapePoints,
        stopTimes);
    System.out.println(points);
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
