package org.onebusaway.transit_data_federation.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

public class GtfsStopsInPolygonMain {
  
  private static GeometryFactory _factory = new GeometryFactory();

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws IOException {

    if (args.length != 2) {
      System.err.println("usage: gtfs_path polygon_path");
      System.exit(-1);
    }

    Geometry points = readPoints(args[1]);
    
    GtfsReader reader = new GtfsReader();
    reader.setDefaultAgencyId("1");
    reader.getEntityClasses().retainAll(Arrays.asList(Stop.class));
    reader.setInputLocation(new File(args[0]));
    reader.addEntityHandler(new EntityHandlerImpl(points));
    reader.run();
  }

  private static Geometry readPoints(String arg) throws NumberFormatException, IOException {
    
    BufferedReader reader = new BufferedReader(new FileReader(arg));
    String line = null;
    
    
    List<Coordinate> points = new ArrayList<Coordinate>();
    
    while((line = reader.readLine()) != null) {
      String[] tokens = line.trim().split("[,\\s]+");
      double lat = Double.parseDouble(tokens[0]);
      double lon = Double.parseDouble(tokens[1]);
      points.add(new Coordinate(lat,lon));
    }
    
    points.add(points.get(0));
    
    LinearRing ring = _factory.createLinearRing(points.toArray(new Coordinate[points.size()]));
    return _factory.createPolygon(ring, new LinearRing[0]);
  }

  private static class EntityHandlerImpl implements EntityHandler {

    private Geometry _geometry;

    public EntityHandlerImpl(Geometry geometry) {
      _geometry = geometry;
    }

    @Override
    public void handleEntity(Object bean) {

      Stop stop = (Stop) bean;

      Point point = _factory.createPoint(new Coordinate(stop.getLat(),stop.getLon()));
      if (_geometry.contains(point))
        System.out.println(stop.getLat() + " " + stop.getLon() + " "
            + stop.getId());
    }

  }
}
