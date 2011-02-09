package org.onebusaway.transit_data_federation.utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.utility.collections.TreeUnionFind;

public class GtfsStopsForMergingMain {

  private static final String ARG_DISTANCE = "distance";

  public static void main(String[] args) throws IOException, ParseException {

    Options options = new Options();
    options.addOption(ARG_DISTANCE, true, "");

    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);
    args = cli.getArgs();

    if (args.length < 3) {
      usage();
      System.exit(-1);
    }

    double distanceThreshold = 20;
    if (cli.hasOption(ARG_DISTANCE))
      distanceThreshold = Double.parseDouble(cli.getOptionValue(ARG_DISTANCE));

    PrintWriter writer = getOutputAsWriter(args[args.length - 1]);

    TreeUnionFind<Stop> union = new TreeUnionFind<Stop>();
    List<Collection<Stop>> allStops = new ArrayList<Collection<Stop>>();
    
    for (int i = 0; i < args.length - 1; i++) {
      
      Collection<Stop> stops = readStopsFromGtfsPath(args[i]);
      
      for (Collection<Stop> previousStops : allStops) {
        for (Stop stopA : previousStops) {
          for (Stop stopB : stops) {
            double d = SphericalGeometryLibrary.distance(stopA.getLat(),
                stopA.getLon(), stopB.getLat(), stopB.getLon());
            if (d < distanceThreshold)
              union.union(stopA, stopB);
          }
        }
      }
      
      allStops.add(stops);
    }

    for (Set<Stop> set : union.getSetMembers()) {
      boolean first = true;
      for (Stop stop : set) {
        if (first)
          first = false;
        else
          writer.print(' ');
        writer.print(AgencyAndIdLibrary.convertToString(stop.getId()));
      }
      writer.println();
      writer.flush();
    }
  }

  private static void usage() {
    System.err.println("usage: gtfs_feed gtfs_feed [...] output");
  }

  private static PrintWriter getOutputAsWriter(String value) throws IOException {
    if (value.equals("-"))
      return new PrintWriter(new OutputStreamWriter(System.out));
    return new PrintWriter(new FileWriter(value));
  }

  private static Collection<Stop> readStopsFromGtfsPath(String path)
      throws IOException {

    String defaultAgencyId = null;
    int index = path.indexOf(File.pathSeparatorChar);
    
    if( index != -1) {
      defaultAgencyId = path.substring(index+1);
      path = path.substring(0,index);
    }
    
    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(path));
    
    if( defaultAgencyId != null)
      reader.setDefaultAgencyId(defaultAgencyId);
    
    reader.readEntities(Agency.class);
    reader.readEntities(Stop.class);
    return reader.getEntityStore().getAllEntitiesForType(Stop.class);
  }
}
