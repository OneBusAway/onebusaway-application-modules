package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TransferPatternsAnalysisTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(TransferPatternsAnalysisTask.class);

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _transitGraphDao;

  @Autowired
  public void setBundle(FederatedTransitDataBundle bundle) {
    _bundle = bundle;
  }

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  public void run() {

    Counter<Pair<StopEntry>> counts = new Counter<Pair<StopEntry>>();

    int fileCount = 0;

    for (File file : _bundle.getAllTransferPatternsPaths()) {
      fileCount++;

      _log.info(file.getName());

      Map<StopEntry, TransferPattern> patterns = readPatternsForFile(file);

      for (TransferPattern pattern : patterns.values()) {
        TransferParent root = new TransferParent(new TransferPatternData());
        pattern.getTransfersForAllStops(root);
        for (TransferNode transfer : root.getTransfers())
          countTransferPairs(transfer, counts);
      }

      if (fileCount % 10 == 0)
        writeCounts(counts);
    }

    writeCounts(counts);
  }

  private Map<StopEntry, TransferPattern> readPatternsForFile(File file) {
    try {
      CompactedTransferPatternFactory factory = new CompactedTransferPatternFactory(
          _transitGraphDao);
      factory.readPatternsFromFile(file);
      return factory.getPatternsByOriginStop();
    } catch (IOException ex) {
      throw new IllegalStateException("error reading file " + file, ex);
    }
  }

  private int countTransferPairs(TransferNode node,
      Counter<Pair<StopEntry>> counts) {

    int count = 0;

    if (node.isExitAllowed())
      count++;

    for (TransferNode transfer : node.getTransfers())
      count += countTransferPairs(transfer, counts);

    counts.increment(node.getStops(), count);

    return count;
  }

  private void writeCounts(Counter<Pair<StopEntry>> counts) {
    try {
      long tIn = System.currentTimeMillis();
      PrintWriter writer = new PrintWriter(
          _bundle.getTransferPatternsSegmentCountsPath());
      List<Pair<StopEntry>> keys = counts.getSortedKeys();
      for (Pair<StopEntry> key : keys) {
        int count = counts.getCount(key);
        String line = CSVLibrary.getAsCSV(count, key.getFirst().getId(),
            key.getSecond().getId());
        writer.println(line);
      }
      writer.close();
      long tOut = System.currentTimeMillis();
      _log.info("Time to write output: " + (tOut - tIn));
    } catch (FileNotFoundException ex) {
      throw new IllegalStateException("error writing output", ex);
    }
  }
}
