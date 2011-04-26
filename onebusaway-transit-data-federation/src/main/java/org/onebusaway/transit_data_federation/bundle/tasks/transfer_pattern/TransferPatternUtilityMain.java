package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

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

  public static void main(String[] args) throws Exception {
    TransferPatternUtilityMain m = new TransferPatternUtilityMain();
    m.run(args);
  }

  public void run(String[] args) throws ParseException, IOException {
    Options options = createOptions();
    Parser parser = new GnuParser();
    CommandLine cli = parser.parse(options, args);
    args = cli.getArgs();

    if (args.length == 0) {
      usage();
      System.exit(-1);
    }

    Set<String> hubStops = loadHubStops(cli);
    Set<String> originStopsWeHaveSeen = new HashSet<String>();

    Map<String, String> idMapping = new HashMap<String, String>();
    Set<String> pruneFromParent = new HashSet<String>();
    Map<String, Integer> depths = new HashMap<String, Integer>();

    long currentIndex = 0;

    for (String arg : args) {

      BufferedReader reader = new BufferedReader(IOLibrary.getPathAsReader(arg));
      String line = null;

      boolean originStopIsHub = false;
      boolean skipTree = false;

      while ((line = reader.readLine()) != null) {

        if (line.length() == 0)
          continue;

        List<String> tokens = CSVLibrary.parse(line);
        String index = tokens.get(0);
        String stopId = tokens.get(1);
        int depth = 0;

        if (tokens.size() == 3) {

          originStopIsHub = hubStops.contains(stopId);

          idMapping.clear();
          pruneFromParent.clear();
          depths.clear();

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

        String newIndex = Long.toString(currentIndex++);
        idMapping.put(index, newIndex);
        tokens.set(0, newIndex);

        if (tokens.size() == 4) {
          String parentIndex = tokens.get(3);
          if (pruneFromParent.contains(parentIndex)) {
            pruneFromParent.add(index);
            continue;
          }
          String newParentIndex = idMapping.get(parentIndex);
          tokens.set(3, newParentIndex);

          depth = depths.get(parentIndex) + 1;
          depths.put(index, depth);
        }

        if (!originStopIsHub && (depth % 2) == 0 && hubStops.contains(stopId)) {
          pruneFromParent.add(index);
          tokens.set(2, "2");
        }

        line = CSVLibrary.getIterableAsCSV(tokens);
        System.out.println(line);
      }

      reader.close();
    }
  }

  private Options createOptions() {
    Options options = new Options();
    options.addOption(ARG_HUB_STOPS, true, "");
    return options;
  }

  private void usage() {
    System.err.println("usage: [-hubStops HubStops.txt] trace [trace ...]");
  }

  private Set<String> loadHubStops(CommandLine cli) throws IOException {

    if (!cli.hasOption(ARG_HUB_STOPS))
      return Collections.emptySet();

    Set<String> hubStops = new HashSet<String>();
    BufferedReader reader = new BufferedReader(new FileReader(
        cli.getOptionValue(ARG_HUB_STOPS)));
    String line = null;

    while ((line = reader.readLine()) != null) {
      hubStops.add(line);
    }

    reader.close();

    return hubStops;
  }
}
