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
package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.Counter;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.transit_data_federation.services.FederatedTransitDataBundle;
import org.onebusaway.utility.IOLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TransferPatternsHubAnalysisTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(TransferPatternsHubAnalysisTask.class);

  private FederatedTransitDataBundle _bundle;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  public void run() {

    Counter<String> counts = new Counter<String>();

    List<File> files = _bundle.getAllTransferPatternsPaths();
    int fileCount = 0;

    for (File file : files) {

      _log.info(file.getName());

      try {
        readTransferStopCountsForFile(file, counts);
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }

      if (fileCount % 10 == 0)
        writeCounts(counts);

      fileCount++;
    }

    writeCounts(counts);
  }

  private void readTransferStopCountsForFile(File path, Counter<String> counts)
      throws IOException {

    Map<String, Integer> depths = new HashMap<String, Integer>();

    BufferedReader reader = IOLibrary.getFileAsBufferedReader(path);
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

  private void writeCounts(Counter<String> counts) {
    try {
      long tIn = System.currentTimeMillis();
      PrintWriter writer = new PrintWriter(
          _bundle.getTransferPatternsTransferPointCountsPath());
      List<String> keys = counts.getSortedKeys();
      for (String key : keys) {
        int count = counts.getCount(key);
        writer.println(count + "\t" + key);
      }
      writer.close();
      long tOut = System.currentTimeMillis();
      _log.info("Time to write output: " + (tOut - tIn));
    } catch (FileNotFoundException ex) {
      throw new IllegalStateException("error writing output", ex);
    }
  }
}
