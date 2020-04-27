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
package org.onebusaway.transit_data_federation.bundle.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.utility.IOLibrary;
import org.onebusaway.utility.collections.TreeUnionFind;
import org.onebusaway.utility.collections.TreeUnionFind.Sentry;

public class GtfsStopsForMergingMain {

  private static final String ARG_CONSOLIDATED = "consolidated";

  private static final String ARG_DISTANCE = "distance";

  public static void main(String[] args) throws IOException, ParseException {

    Options options = new Options();
    options.addOption(ARG_DISTANCE, true, "");
    options.addOption(ARG_CONSOLIDATED, true, "");

    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);
    args = cli.getArgs();

    if (args.length < 5) {
      usage();
      System.exit(-1);
    }

    double distanceThreshold = 20;
    if (cli.hasOption(ARG_DISTANCE))
      distanceThreshold = Double.parseDouble(cli.getOptionValue(ARG_DISTANCE));

    TreeUnionFind<AgencyAndId> existingConsolidatedStops = new TreeUnionFind<AgencyAndId>();
    if (cli.hasOption(ARG_CONSOLIDATED))
      existingConsolidatedStops = loadExistingConsolidatedStops(cli.getOptionValue(ARG_CONSOLIDATED));

    PrintWriter writer = getOutputAsWriter(args[args.length - 1]);

    TreeUnionFind<AgencyAndId> union = new TreeUnionFind<AgencyAndId>();
    List<Collection<Stop>> allStops = new ArrayList<Collection<Stop>>();

    for (int i = 0; i < args.length - 1; i += 2) {
      String path = args[i];
      String agencyId = args[i + 1];
      Collection<Stop> stops = readStopsFromGtfsPath(path, agencyId);

      for (Collection<Stop> previousStops : allStops) {
        for (Stop stopA : previousStops) {
          for (Stop stopB : stops) {
            double d = SphericalGeometryLibrary.distance(stopA.getLat(),
                stopA.getLon(), stopB.getLat(), stopB.getLon());
            if (d < distanceThreshold
                && !existingConsolidatedStops.isSameSet(stopA.getId(),
                    stopB.getId())) {
              union.union(stopA.getId(), stopB.getId());
            }
          }
        }
      }

      allStops.add(stops);
    }

    for (Set<AgencyAndId> set : union.getSetMembers()) {
      Set<Sentry> existing = new HashSet<Sentry>();
      boolean first = true;
      for (AgencyAndId stopId : set) {
        if (first)
          first = false;
        else
          writer.print(' ');
        if (existingConsolidatedStops.contains(stopId))
          existing.add(existingConsolidatedStops.find(stopId));
        writer.print(AgencyAndIdLibrary.convertToString(stopId));
      }
      writer.println();
      for (Sentry sentry : existing)
        writer.println("  => " + existingConsolidatedStops.members(sentry));
      writer.flush();
    }
  }

  private static void usage() {
    System.err.println("usage: gtfs_feed agencyId gtfs_feed agencyId [...] output");
  }

  private static PrintWriter getOutputAsWriter(String value) throws IOException {
    if (value.equals("-"))
      return new PrintWriter(new OutputStreamWriter(System.out));
    return new PrintWriter(new FileWriter(value));
  }

  private static Collection<Stop> readStopsFromGtfsPath(String path,
      String defaultAgencyId) throws IOException {

    GtfsReader reader = new GtfsReader();
    reader.setInputLocation(new File(path));

    if (defaultAgencyId != null)
      reader.setDefaultAgencyId(defaultAgencyId);

    reader.readEntities(Agency.class);
    reader.readEntities(Stop.class);
    return reader.getEntityStore().getAllEntitiesForType(Stop.class);
  }

  private static TreeUnionFind<AgencyAndId> loadExistingConsolidatedStops(
      String path) throws IOException {

    InputStream in = IOLibrary.getPathAsInputStream(path);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    TreeUnionFind<AgencyAndId> consolidated = new TreeUnionFind<AgencyAndId>();

    String line = null;

    while ((line = reader.readLine()) != null) {
      if (line.startsWith("#") || line.startsWith("{{{")
          || line.startsWith("}}}") || line.length() == 0)
        continue;
      String[] tokens = line.split("\\s+");
      AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(tokens[0]);
      for (int i = 1; i < tokens.length; i++) {
        AgencyAndId otherStopId = AgencyAndIdLibrary.convertFromString(tokens[i]);
        consolidated.union(stopId, otherStopId);
      }
    }

    reader.close();

    return consolidated;
  }
}
