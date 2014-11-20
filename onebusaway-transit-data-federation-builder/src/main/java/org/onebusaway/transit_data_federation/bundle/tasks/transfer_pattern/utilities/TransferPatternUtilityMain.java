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
package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.utility.IOLibrary;

public class TransferPatternUtilityMain {

  private static final String ARG_HUB_STOPS = "hubStops";

  private static final String ARG_PRUNE_DIRECT_TRANSFERS = "pruneDirectTransfers";

  public static void main(String[] args) throws Exception {
    TransferPatternUtilityMain m = new TransferPatternUtilityMain();
    m.run(args);
  }

  private long _currentIndex = 0;

  private Map<String, String> _idMapping = new HashMap<String, String>();

  public void run(String[] args) throws ParseException, IOException {
    Options options = createOptions();
    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);
    args = cli.getArgs();

    if (args.length == 0) {
      usage();
      System.exit(-1);
    }

    boolean pruneDirectTransfers = cli.hasOption(ARG_PRUNE_DIRECT_TRANSFERS);

    Map<String, Integer> hubStops = loadHubStops(cli);
    Set<String> originStopsWeHaveSeen = new HashSet<String>();

    Set<String> pruneFromParent = new HashSet<String>();
    Map<String, Integer> depths = new HashMap<String, Integer>();

    Map<String, String> directTransfers = new HashMap<String, String>();

    for (String arg : args) {

      System.err.println("# " + arg);

      BufferedReader reader = new BufferedReader(IOLibrary.getPathAsReader(arg));
      String line = null;

      int originHubDepth = Integer.MAX_VALUE;
      String originIndex = null;
      boolean skipTree = false;

      while ((line = reader.readLine()) != null) {

        if (line.length() == 0)
          continue;

        List<String> tokens = new CSVLibrary().parse(line);
        String index = tokens.get(0);
        String stopId = tokens.get(1);
        String type = tokens.get(2);
        String parentIndex = null;
        if (tokens.size() > 3)
          parentIndex = tokens.get(3);

        int depth = 0;
        boolean isOrigin = false;

        if (parentIndex == null) {

          isOrigin = true;
          originHubDepth = hubDepth(hubStops, stopId);

          _idMapping.clear();
          pruneFromParent.clear();
          depths.clear();
          
          directTransfers.clear();

          depths.put(index, 0);

          skipTree = false;

          if (originStopsWeHaveSeen.add(stopId)) {
            System.err.println("#   Origin Stop: " + stopId);
          } else {
            System.err.println("#   Skipping duplicate origin stop: " + stopId);
            skipTree = true;
          }
        }

        if (skipTree)
          continue;

        if (parentIndex != null) {
          if (pruneFromParent.contains(parentIndex)) {
            pruneFromParent.add(index);
            continue;
          }

          /**
           * If we had an uncommited parent, commit it
           */
          String parentStopId = directTransfers.remove(parentIndex);
          if (parentStopId != null)
            commitLine(parentIndex, parentStopId, "0", originIndex);

          depth = depths.get(parentIndex) + 1;
          depths.put(index, depth);

          String newParentIndex = _idMapping.get(parentIndex);
          tokens.set(3, newParentIndex);
          parentIndex = newParentIndex;
        }

        int hubDepth = hubDepth(hubStops, stopId);
        if ((depth % 2) == 0 && hubDepth < originHubDepth) {
          pruneFromParent.add(index);
          type = "2";
        }

        if (pruneDirectTransfers && depth == 1) {
          directTransfers.put(index, stopId);
        } else {
          String newIndex = commitLine(index, stopId, type, parentIndex);

          if (isOrigin)
            originIndex = newIndex;
        }
      }

      reader.close();
    }
    
    
  }

  private String commitLine(String index, String stopId, String type,
      String parentIndex) {
    String newIndex = Long.toString(_currentIndex);
    _idMapping.put(index, newIndex);

    String newLine = parentIndex == null ? CSVLibrary.getAsCSV(newIndex,
        stopId, type)
        : CSVLibrary.getAsCSV(newIndex, stopId, type, parentIndex);
    System.out.println(newLine);
    _currentIndex++;
    return newIndex;
  }

  private int hubDepth(Map<String, Integer> hubStops, String stopId) {
    int hubDepth = Integer.MAX_VALUE;
    if (hubStops.containsKey(stopId))
      hubDepth = hubStops.get(stopId);
    return hubDepth;
  }

  private Options createOptions() {
    Options options = new Options();
    options.addOption(ARG_HUB_STOPS, true, "");
    options.addOption(ARG_PRUNE_DIRECT_TRANSFERS, false, "");
    return options;
  }

  private void usage() {
    System.err.println("usage: [-hubStops HubStops.txt] [-pruneDirectTransfers] trace [trace ...]");
  }

  private Map<String, Integer> loadHubStops(CommandLine cli) throws IOException {

    if (!cli.hasOption(ARG_HUB_STOPS))
      return Collections.emptyMap();

    Map<String, Integer> hubStops = new HashMap<String, Integer>();
    BufferedReader reader = new BufferedReader(new FileReader(
        cli.getOptionValue(ARG_HUB_STOPS)));
    String line = null;

    while ((line = reader.readLine()) != null) {
      List<String> tokens = new CSVLibrary().parse(line);
      String stopId = tokens.get(0);
      int depth = 0;
      if (tokens.size() > 1)
        depth = Integer.parseInt(tokens.get(1));
      if (!hubStops.containsKey(stopId))
        hubStops.put(stopId, depth);
    }

    reader.close();

    return hubStops;
  }
}
