package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.bundle.model.FederatedTransitDataBundle;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.utility.IOLibrary;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SerializedTransferPatternsTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(SerializedTransferPatternsTask.class);

  private FederatedTransitDataBundle _bundle;

  private TransitGraphDao _transitGraphDao;

  private final Map<AgencyAndId, CompactedTransferPattern> _patternsByStopId = new HashMap<AgencyAndId, CompactedTransferPattern>();

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

    /**
     * Clear out any existing files
     */
    deleteExistingFile();

    List<File> paths = _bundle.getAllTransferPatternsPaths();
    int pathIndex = 0;

    CompactedTransferPatternFactory factory = new CompactedTransferPatternFactory(
        _transitGraphDao);
    factory.addListener(new FactoryListener());

    Set<StopEntry> hubStops = loadHubStops();
    factory.setHubStops(hubStops);

    for (File path : paths) {
      if (pathIndex % 10 == 0)
        _log.info("transfer pattern files processed: " + pathIndex + "/"
            + paths.size());
      pathIndex++;

      try {

        factory.readPatternsFromFile(path);

      } catch (IOException ex) {
        throw new IllegalStateException(
            "error processing transfer pattern paths in file " + path, ex);
      }
    }

    writeOutput();
  }

  private void deleteExistingFile() {
    File path = _bundle.getSerializedTransferPatternsPath();
    path.delete();
  }

  private Set<StopEntry> loadHubStops() {

    File hubStopsFile = _bundle.getHubStopsPath(false);
    if (!hubStopsFile.exists())
      return Collections.emptySet();

    Set<StopEntry> hubStops = new HashSet<StopEntry>();

    try {
      BufferedReader reader = IOLibrary.getFileAsBufferedReader(hubStopsFile);
      String line = null;

      while ((line = reader.readLine()) != null) {
        String stopId = line;
        int index = stopId.indexOf('\t');
        if (index != -1)
          stopId = stopId.substring(index + 1);
        AgencyAndId id = AgencyAndIdLibrary.convertFromString(stopId);
        StopEntry stop = _transitGraphDao.getStopEntryForId(id, true);
        hubStops.add(stop);
      }

      reader.close();

      return hubStops;

    } catch (IOException ex) {
      throw new IllegalStateException("error loading file " + hubStopsFile, ex);
    }
  }

  private void writeOutput() {
    try {
      File path = _bundle.getSerializedTransferPatternsPath();
      ObjectSerializationLibrary.writeObject(path, _patternsByStopId);
      _patternsByStopId.clear();
    } catch (Exception ex) {
      throw new IllegalStateException("error serializing output", ex);
    }
  }

  private class FactoryListener implements
      CompactedTransferPatternFactoryListener {

    @Override
    public void patternProcessed(CompactedTransferPatternFactory factory,
        StopEntry originStop, CompactedTransferPattern pattern) {
      _patternsByStopId.put(originStop.getId(), pattern);
      factory.clear();
    }
  }
}
