package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.utility.IOLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class TransferPatternsCompressionTask implements Runnable {

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

  @Override
  public void run() {

    List<StopEntry> hubs = loadHubs();

    int fileIndex = 0;

    SortedMap<Integer, DoubleArrayList> nodeCounts = new TreeMap<Integer, DoubleArrayList>();
    SortedMap<Integer, DoubleArrayList> hubCounts = new TreeMap<Integer, DoubleArrayList>();

    nodeCounts = FactoryMap.createSorted(nodeCounts, new DoubleArrayList());
    hubCounts = FactoryMap.createSorted(hubCounts, new DoubleArrayList());

    for (File file : _bundle.getAllTransferPatternsPaths()) {

      _log.info(file.getName());
      fileIndex++;

      Map<StopEntry, TransferPattern> patterns = readPatternsForFile(file);

      for (TransferPattern pattern : patterns.values()) {

        StopEntry originStop = pattern.getOriginStop();

        TransferParent root = new TransferParent(new TransferPatternData());
        pattern.getTransfersForAllStops(root);

        Set<StopEntry> activeHubs = new HashSet<StopEntry>();
        for (int i = 0; i < hubs.size(); i++) {

          activeHubs.add(hubs.get(hubs.size() - i - 1));
          if (activeHubs.contains(originStop))
            break;

          if (i % 200 == 0) {
            Set<StopEntry> hubsWeSaw = new HashSet<StopEntry>();

            int c = 0;

            for (TransferNode node : root.getTransfers())
              c += visitNode(node, activeHubs, hubsWeSaw);

            nodeCounts.get(i).add(c);
            hubCounts.get(i).add(hubsWeSaw.size());
          }
        }
      }

      if (fileIndex % 10 == 0)
        dumpStats(nodeCounts, hubCounts);
    }

    dumpStats(nodeCounts, hubCounts);
  }

  private void dumpStats(SortedMap<Integer, DoubleArrayList> nodeCounts,
      SortedMap<Integer, DoubleArrayList> hubCounts) {

    for (int i : nodeCounts.keySet()) {
      DoubleArrayList nodeCount = nodeCounts.get(i);
      DoubleArrayList hubCount = hubCounts.get(i);
      double nodeMean = Descriptive.mean(nodeCount);
      double hubMean = Descriptive.mean(hubCount);
      System.out.println(i + "," + nodeCount.size() + "," + nodeMean + ","
          + hubMean);
    }
  }

  private int visitNode(TransferNode node, Set<StopEntry> activeHubs,
      Set<StopEntry> hubsWeSaw) {

    StopEntry stop = node.getFromStop();

    if (activeHubs.contains(stop)) {
      hubsWeSaw.add(stop);
      return 0;
    }

    int c = 1;

    for (TransferNode child : node.getTransfers())
      c += visitNode(child, activeHubs, hubsWeSaw);

    return c;
  }

  private List<StopEntry> loadHubs() {
    try {
      List<StopEntry> hubs = new ArrayList<StopEntry>();
      BufferedReader reader = IOLibrary.getPathAsBufferedReader(_bundle.getHubStopsPath(
          false).getAbsolutePath());
      String line = null;
      while ((line = reader.readLine()) != null) {
        AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(line);
        StopEntry stop = _transitGraphDao.getStopEntryForId(stopId, true);
        hubs.add(stop);
      }

      reader.close();
      return hubs;

    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
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
}
