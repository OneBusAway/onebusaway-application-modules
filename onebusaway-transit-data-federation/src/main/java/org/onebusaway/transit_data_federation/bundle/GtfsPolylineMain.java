package org.onebusaway.transit_data_federation.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.onebusaway.container.ContainerLibrary;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.springframework.context.ConfigurableApplicationContext;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import edu.washington.cs.rse.geospatial.GeoPoint;
import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.geospatial.UTMLibrary;
import edu.washington.cs.rse.geospatial.UTMProjection;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

public class GtfsPolylineMain {

  private static final String ARG_FORMAT = "format";

  private enum EFormat {
    OSM, XML, ENCODED
  };

  public static void main(String[] args) throws IOException,
      ClassNotFoundException {

    GtfsPolylineMain main = new GtfsPolylineMain();
    main.run(args);
  }

  public void run(String[] args) throws IOException {

    try {
      Parser parser = new GnuParser();

      Options options = new Options();
      buildOptions(options);

      CommandLine commandLine = parser.parse(options, args);

      String[] remainingArgs = commandLine.getArgs();

      if (remainingArgs.length < 2) {
        printUsage();
        System.exit(-1);
      }

      List<String> contextPaths = new ArrayList<String>();
      for (int i = 0; i < remainingArgs.length - 1; i++)
        contextPaths.add("file:" + remainingArgs[i]);
      ConfigurableApplicationContext context = ContainerLibrary.createContext(contextPaths);

      File outputPath = new File(remainingArgs[remainingArgs.length - 1]);

      EFormat format = getFormat(commandLine);

      GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");

      StopToPolygonEntityHandler handler = new StopToPolygonEntityHandler(2500);

      for (GtfsBundle bundle : bundles.getBundles()) {
        System.out.println(bundle.getPath());
        GtfsReader reader = new GtfsReader();
        reader.addEntityHandler(handler);
        reader.setInputLocation(bundle.getPath());
        if (bundle.getDefaultAgencyId() != null)
          reader.setDefaultAgencyId(bundle.getDefaultAgencyId());
        reader.readEntities(Stop.class);
      }

      Geometry geometry = handler.getGeometry();
      UTMProjection proj = handler.getProjection();

      System.out.println("polygon");
      AtomicInteger index = new AtomicInteger();
      printGeometry(geometry, proj, index);
      System.out.println("END");

    } catch (ParseException ex) {
      System.err.println(ex.getLocalizedMessage());
      printUsage();
      System.exit(-1);
    }

    System.exit(0);
  }

  private void printGeometry(Geometry geometry, UTMProjection proj,
      AtomicInteger index) {
    if (geometry instanceof Polygon)
      printPolygon((Polygon) geometry, proj, index);
    else if (geometry instanceof MultiPolygon)
      printMultiPolygon((MultiPolygon) geometry, proj, index);
    else
      System.out.println("unknown geometry: " + geometry);
  }

  private void printMultiPolygon(MultiPolygon multi, UTMProjection proj,
      AtomicInteger index) {
    for (int i = 0; i < multi.getNumGeometries(); i++)
      printGeometry(multi.getGeometryN(i), proj, index);
  }

  private void printPolygon(Polygon poly, UTMProjection proj,
      AtomicInteger index) {
    System.out.println(index.incrementAndGet());
    printLineString(proj, poly.getExteriorRing());
    System.out.println("END");
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      System.out.println("!" + index.incrementAndGet());
      printLineString(proj, poly.getInteriorRingN(i));
      System.out.println("END");
    }
  }

  private void printLineString(UTMProjection proj, LineString line) {
    for (int i = 0; i < line.getNumPoints(); i++) {
      Point point = line.getPointN(i);
      GeoPoint p = new GeoPoint(proj, point.getX(), point.getY(), 0);
      CoordinatePoint c = p.getCoordinates();
      System.out.println(c.getLon() + " " + c.getLat());
    }
  }

  protected void buildOptions(Options options) {
    options.addOption(ARG_FORMAT, true, "");
  }

  protected void printUsage() {
    System.err.println("usage: [-format osm|xml|encoded] data-sources.xml [data-sources.xml ...] output_directory");
  }

  protected EFormat getFormat(CommandLine cli) {
    if (!cli.hasOption(ARG_FORMAT))
      return EFormat.XML;
    String format = cli.getOptionValue(ARG_FORMAT);
    if (format.equals("osm"))
      return EFormat.OSM;
    else if (format.equals("xml"))
      return EFormat.XML;
    else if (format.equals("encoded"))
      return EFormat.ENCODED;
    throw new IllegalStateException("unknown format: " + format);
  }

  private static class StopToPolygonEntityHandler implements EntityHandler {

    private GeometryFactory _factory = new GeometryFactory();

    private UTMProjection _projection;

    private double _bufferRadiusInMeters;

    private Geometry _geometry;

    public StopToPolygonEntityHandler(double bufferRadiusInMeters) {
      _bufferRadiusInMeters = bufferRadiusInMeters;
    }

    public Geometry getGeometry() {
      return _geometry;
    }

    public UTMProjection getProjection() {
      return _projection;
    }

    @Override
    public void handleEntity(Object bean) {

      Stop stop = (Stop) bean;

      if (_projection == null) {
        int zone = UTMLibrary.getUTMIndexForLatitude(stop.getLat());
        _projection = new UTMProjection(zone);
      }

      IGeoPoint point = _projection.forward(new CoordinatePoint(stop.getLat(),
          stop.getLon()));

      Point p = _factory.createPoint(new Coordinate(point.getX(), point.getY()));
      Geometry geometry = p.buffer(_bufferRadiusInMeters).getEnvelope();

      if (_geometry == null)
        _geometry = geometry;
      else
        _geometry = _geometry.union(geometry);
    }
  }
}
