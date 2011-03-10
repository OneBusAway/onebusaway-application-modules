package org.onebusaway.transit_data_federation.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

/**
 * Utility script to compute a polygon boundary for the set of stops in a GTFS,
 * useful for passing to an OpenStreetMap script for extracting an appropriate
 * subset of a larger OSM document.
 * 
 * @author bdferris
 * 
 */
public class ComputePolylineBoundaryForGtfsStopsMain {

  private static final String ARG_FORMAT = "format";

  private enum EFormat {
    OSM, XML, ENCODED, TEXT
  };

  public static void main(String[] args) throws IOException,
      ClassNotFoundException {

    ComputePolylineBoundaryForGtfsStopsMain main = new ComputePolylineBoundaryForGtfsStopsMain();
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

      List<GtfsBundle> bundles = getGtfsBundlesFromCommandLine(remainingArgs);
      EFormat format = getFormat(commandLine);

      StopToPolygonEntityHandler handler = new StopToPolygonEntityHandler(2500);

      for (GtfsBundle bundle : bundles) {
        System.err.println(bundle.getPath());
        GtfsReader reader = new GtfsReader();
        reader.addEntityHandler(handler);
        reader.setInputLocation(bundle.getPath());
        if (bundle.getDefaultAgencyId() != null)
          reader.setDefaultAgencyId(bundle.getDefaultAgencyId());
        reader.readEntities(Stop.class);
      }

      PrintWriter out = getOutputAsPrinter(remainingArgs[remainingArgs.length - 1]);

      switch (format) {
        case OSM:
          handleOutputAsOSMPolygon(out, handler);
          break;
        case TEXT:
          handleOutputAsText(out, handler);
          break;
      }

      out.close();

    } catch (ParseException ex) {
      System.err.println(ex.getLocalizedMessage());
      printUsage();
      System.exit(-1);
    }

    System.exit(0);
  }

  private PrintWriter getOutputAsPrinter(String path) throws IOException {
    if (path.equals("-"))
      return new PrintWriter(new OutputStreamWriter(System.out));
    return new PrintWriter(new FileWriter(path));
  }

  private void handleOutputAsOSMPolygon(PrintWriter out,
      StopToPolygonEntityHandler handler) throws IOException {

    Geometry geometry = handler.getGeometry();
    UTMProjection proj = handler.getProjection();

    out.println("polygon");
    AtomicInteger index = new AtomicInteger();
    printGeometry(out, geometry, proj, index, false);
    out.println("END");

  }

  private void handleOutputAsText(PrintWriter out,
      StopToPolygonEntityHandler handler) {

    Geometry geometry = handler.getGeometry();
    UTMProjection proj = handler.getProjection();

    out.println("polygon");
    AtomicInteger index = new AtomicInteger();
    printGeometry(out, geometry, proj, index, true);
    out.println("END");
  }

  private List<GtfsBundle> getGtfsBundlesFromCommandLine(String[] args) {

    List<GtfsBundle> allBundles = new ArrayList<GtfsBundle>();
    List<String> contextPaths = new ArrayList<String>();

    for (int i = 0; i < args.length - 1; i++) {
      if (args[i].endsWith(".xml"))
        contextPaths.add("file:" + args[i]);
      else {
        GtfsBundle bundle = new GtfsBundle();
        bundle.setPath(new File(args[i]));
        bundle.setDefaultAgencyId(Integer.toString(i));
        allBundles.add(bundle);
      }
    }

    if (!contextPaths.isEmpty()) {
      ConfigurableApplicationContext context = ContainerLibrary.createContext(contextPaths);
      GtfsBundles bundles = (GtfsBundles) context.getBean("gtfs-bundles");
      allBundles.addAll(bundles.getBundles());
    }

    return allBundles;
  }

  private void printGeometry(PrintWriter out, Geometry geometry,
      UTMProjection proj, AtomicInteger index, boolean latFirst) {
    if (geometry instanceof Polygon)
      printPolygon(out, (Polygon) geometry, proj, index, latFirst);
    else if (geometry instanceof MultiPolygon)
      printMultiPolygon(out, (MultiPolygon) geometry, proj, index, latFirst);
    else
      System.err.println("unknown geometry: " + geometry);
  }

  private void printMultiPolygon(PrintWriter out, MultiPolygon multi,
      UTMProjection proj, AtomicInteger index, boolean latFirst) {
    for (int i = 0; i < multi.getNumGeometries(); i++)
      printGeometry(out, multi.getGeometryN(i), proj, index, latFirst);
  }

  private void printPolygon(PrintWriter out, Polygon poly, UTMProjection proj,
      AtomicInteger index, boolean latFirst) {
    out.println(index.incrementAndGet());
    printLineString(out, proj, poly.getExteriorRing(), latFirst);
    out.println("END");
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      out.println("!" + index.incrementAndGet());
      printLineString(out, proj, poly.getInteriorRingN(i), latFirst);
      out.println("END");
    }
  }

  private void printLineString(PrintWriter out, UTMProjection proj,
      LineString line, boolean latFirst) {
    for (int i = 0; i < line.getNumPoints(); i++) {
      Point point = line.getPointN(i);
      GeoPoint p = new GeoPoint(proj, point.getX(), point.getY(), 0);
      CoordinatePoint c = p.getCoordinates();
      if (latFirst)
        out.println(c.getLat() + " " + c.getLon());
      else
        out.println(c.getLon() + " " + c.getLat());
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
    else if (format.equals("text"))
      return EFormat.TEXT;
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
