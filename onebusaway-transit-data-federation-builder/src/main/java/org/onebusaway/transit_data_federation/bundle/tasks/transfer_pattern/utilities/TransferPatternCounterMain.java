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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.onebusaway.collections.Counter;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.IOLibrary;

public class TransferPatternCounterMain {

  public static void main(String[] args) throws Exception {
    TransferPatternCounterMain m = new TransferPatternCounterMain();
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

    Map<String, Integer> depths = new HashMap<String, Integer>();
    Counter<String> counts = new Counter<String>();

    List<String> paths = new ArrayList<String>();
    for (String arg : args) {
      File f = new File(arg);
      if (f.isDirectory()) {
        FederatedTransitDataBundle bundle = new FederatedTransitDataBundle(f);
        for (File path : bundle.getAllTransferPatternsPaths())
          paths.add(path.getAbsolutePath());
      } else {
        paths.add(arg);
      }
    }

    for (String path : paths) {

      System.err.println("# " + path);

      BufferedReader reader = new BufferedReader(
          IOLibrary.getPathAsReader(path));
      String line = null;

      while ((line = reader.readLine()) != null) {

        if (line.length() == 0)
          continue;

        List<String> tokens = new CSVLibrary().parse(line);
        String index = tokens.get(0);
        String stopId = tokens.get(1);
        String parentIndex = null;
        if (tokens.size() > 3)
          parentIndex = tokens.get(3);

        int depth = 0;

        if (parentIndex == null) {
          depths.clear();
          depths.put(index, 0);
        }

        if (parentIndex != null) {
          depth = depths.get(parentIndex) + 1;
          depths.put(index, depth);
        }

        if (depth > 0 && depth % 2 == 0)
          counts.increment(stopId);
      }

      reader.close();
    }

    List<String> keys = counts.getSortedKeys();
    for (String key : keys)
      System.out.println(counts.getCount(key) + "\t" + key);
  }

  private Options createOptions() {
    Options options = new Options();
    return options;
  }

  private void usage() {
    System.err.println("usage: trace [trace ...]");
  }
}
