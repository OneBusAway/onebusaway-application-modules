package org.onebusaway.transit_data_federation.bundle.tasks.transfer_pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompactedTransferPatternFactory {

  private static Logger _log = LoggerFactory.getLogger(CompactedTransferPatternFactory.class);

  private final Map<StopEntry, TransferPattern> _patternsByOriginStop = new HashMap<StopEntry, TransferPattern>();

  private final TransitGraphDao _dao;

  public CompactedTransferPatternFactory(TransitGraphDao dao) {
    _dao = dao;
  }

  public Map<StopEntry, TransferPattern> getPatternsByOriginStop() {
    return _patternsByOriginStop;
  }

  public void readPatternsFromFile(File path) throws IOException {
    
    BufferedReader reader = openFile(path);
    String line = null;

    List<StopEntry> stops = new ArrayList<StopEntry>();
    List<Integer> parentIndices = new ArrayList<Integer>();
    Map<StopEntry, List<Integer>> leafIndices = new FactoryMap<StopEntry, List<Integer>>(
        new ArrayList<Integer>());

    Map<Long, Integer> indicesByKey = new HashMap<Long, Integer>();

    while ((line = reader.readLine()) != null) {

      List<String> tokens = CSVLibrary.parse(line);

      if (tokens.size() == 3) {
        compact(stops, parentIndices, leafIndices);
        indicesByKey.clear();
      }

      int index = stops.size();

      long key = Long.parseLong(tokens.get(0));

      AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(tokens.get(1));
      StopEntry stop = _dao.getStopEntryForId(stopId, true);

      boolean endpoint = tokens.get(2).equals("1");

      stops.add(stop);
      indicesByKey.put(key, index);

      if (endpoint)
        leafIndices.get(stop).add(index);

      if (tokens.size() == 4) {
        long parentKey = Long.parseLong(tokens.get(3));
        int parentIndex = indicesByKey.get(parentKey);
        parentIndices.add(parentIndex);
      } else {
        parentIndices.add(-1);
      }
    }

    if (!stops.isEmpty())
      compact(stops, parentIndices, leafIndices);

    reader.close();
  }

  /****
   * Private Methods
   ****/

  private BufferedReader openFile(File path) throws IOException {
    InputStream in = new FileInputStream(path);
    if (path.getName().endsWith(".gz"))
      in = new GZIPInputStream(in);
    return new BufferedReader(new InputStreamReader(in));
  }

  private void compact(List<StopEntry> stops, List<Integer> parentIndices,
      Map<StopEntry, List<Integer>> leafIndicesByStop) {

    if (stops.isEmpty())
      return;

    StopEntry[] stopArray = new StopEntry[stops.size()];
    int[] parentIndicesArray = new int[stops.size()];
    Map<StopEntry, int[]> leafIndices = new HashMap<StopEntry, int[]>();

    for (int i = 0; i < stops.size(); i++) {
      stopArray[i] = stops.get(i);
      parentIndicesArray[i] = parentIndices.get(i);
    }

    for (Map.Entry<StopEntry, List<Integer>> entry : leafIndicesByStop.entrySet()) {
      StopEntry stop = entry.getKey();
      List<Integer> indices = entry.getValue();
      int[] array = new int[indices.size()];
      for (int i = 0; i < indices.size(); i++)
        array[i] = indices.get(i);
      leafIndices.put(stop, array);
    }

    CompactedTransferPattern pattern = new CompactedTransferPattern(stopArray,
        parentIndicesArray, leafIndices);
    StopEntry originStop = pattern.getOriginStop();

    TransferPattern existing = _patternsByOriginStop.put(originStop, pattern);
    if (existing != null)
      _log.warn("overriding pattern for stop " + originStop.getId());

    stops.clear();
    parentIndices.clear();
    leafIndicesByStop.clear();
  }
}
