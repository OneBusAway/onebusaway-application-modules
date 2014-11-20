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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.onebusaway.csv_entities.CSVLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.tripplanner.CompactedTransferPattern;
import org.onebusaway.transit_data_federation.services.tripplanner.TransferPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompactedTransferPatternFactory {

  private static Logger _log = LoggerFactory.getLogger(CompactedTransferPatternFactory.class);

  private final List<CompactedTransferPatternFactoryListener> _listeners = new ArrayList<CompactedTransferPatternFactoryListener>();

  private final Map<StopEntry, TransferPattern> _patternsByOriginStop = new HashMap<StopEntry, TransferPattern>();

  private final TransitGraphDao _dao;

  private long _lines = 0;

  private List<StopEntry> _allStops;

  private Map<StopEntry, Short> _indices = new HashMap<StopEntry, Short>();

  private Set<StopEntry> _hubStops = Collections.emptySet();

  private Set<String> _pruneFromParent = new HashSet<String>();

  private Map<String, Integer> _depths = new HashMap<String, Integer>();

  public CompactedTransferPatternFactory(TransitGraphDao dao) {
    _dao = dao;
    _allStops = dao.getAllStops();
    if (_allStops.size() > Short.MAX_VALUE)
      throw new IllegalStateException("more than " + Short.MAX_VALUE
          + " stops means our indexing trick will no long work");
    for (short in = (short) 0; in < _allStops.size(); in++)
      _indices.put(_allStops.get(in), in);
  }

  public void addListener(CompactedTransferPatternFactoryListener listener) {
    _listeners.add(listener);
  }

  public void setHubStops(Set<StopEntry> hubStops) {
    _hubStops = hubStops;
  }

  public Map<StopEntry, TransferPattern> getPatternsByOriginStop() {
    return _patternsByOriginStop;
  }

  public void clear() {
    _patternsByOriginStop.clear();
  }

  public void readPatternsFromFile(File path) throws IOException {

    BufferedReader reader = openFile(path);
    String line = null;

    List<Record> records = new ArrayList<Record>();

    StopEntry originStop = null;
    boolean isOriginHubStop = false;

    while ((line = reader.readLine()) != null) {

      if (_lines % 1000000 == 0)
        _log.info("lines=" + _lines);

      if (line.length() == 0)
        continue;

      List<String> tokens = new CSVLibrary().parse(line);

      AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(tokens.get(1));
      StopEntry stop = _dao.getStopEntryForId(stopId, true);
      short stopIndex = _indices.get(stop);

      String key = tokens.get(0);
      ERecordType type = getRecordTypeForValue(tokens.get(2));

      int depth = 0;

      if (tokens.size() == 3) {
        compact(originStop, records);
        originStop = stop;
        isOriginHubStop = _hubStops.contains(originStop);
        _pruneFromParent.clear();
        _depths.clear();
        _depths.put(key, 0);
      }

      String parentKey = null;
      if (tokens.size() == 4) {

        parentKey = tokens.get(3);

        if (_pruneFromParent.contains(parentKey)) {
          _pruneFromParent.add(key);
          continue;
        }

        if( ! _depths.containsKey(parentKey))
          System.out.println("here");
        
        depth = _depths.get(parentKey) + 1;
        _depths.put(key, depth);
      }

      if (!isOriginHubStop && _hubStops.contains(stop) && depth > 0
          && (depth % 2) == 0) {
        _pruneFromParent.add(key);
        type = ERecordType.HUB;
      }

      Record record = new Record(key, stopIndex, parentKey, type);
      records.add(record);

      _lines++;
    }

    if (!records.isEmpty())
      compact(originStop, records);

    reader.close();
  }

  private ERecordType getRecordTypeForValue(String v) {
    if (v.equals("0"))
      return ERecordType.NODE;
    if (v.equals("1"))
      return ERecordType.EXIT_ALLOWED;
    if (v.equals("2"))
      return ERecordType.HUB;
    throw new IllegalStateException("uknown record type");
  }

  public long getLines() {
    return _lines;
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

  private void compact(StopEntry originStop, List<Record> records) {

    if (records.isEmpty())
      return;

    Collections.sort(records);

    Map<String, Integer> offsets = new HashMap<String, Integer>();
    for (Record record : records)
      offsets.put(record.key, offsets.size());

    short[] stopIndexArray = new short[records.size()];
    int[] parentIndicesArray = new int[records.size()];

    int exitAllowedOffset = records.size();
    int hubOffset = records.size();;

    for (int i = 0; i < records.size(); i++) {
      Record record = records.get(i);
      stopIndexArray[i] = record.stopIndex;
      int offset = -1;
      if (record.parentKey != null)
        offset = offsets.get(record.parentKey);
      parentIndicesArray[i] = offset;
      switch (record.type) {
        case NODE:
          exitAllowedOffset = i + 1;
          hubOffset = i + 1;
          break;
        case EXIT_ALLOWED:
          hubOffset = i + 1;
          break;
      }
    }

    if (exitAllowedOffset == -1 || hubOffset == -1)
      throw new IllegalStateException();

    CompactedTransferPattern pattern = new CompactedTransferPattern(
        stopIndexArray, parentIndicesArray, exitAllowedOffset, hubOffset);
    pattern.setAllStops(_allStops);

    TransferPattern existing = _patternsByOriginStop.put(originStop, pattern);
    if (existing != null)
      _log.warn("overriding pattern for stop " + originStop.getId());

    for (CompactedTransferPatternFactoryListener listener : _listeners)
      listener.patternProcessed(this, originStop, pattern);

    records.clear();
  }

  private enum ERecordType {
    NODE, EXIT_ALLOWED, HUB
  }

  private static class Record implements Comparable<Record> {
    private final String key;
    private final short stopIndex;
    private final String parentKey;
    private final ERecordType type;

    public Record(String key, short stopIndex, String parentKey,
        ERecordType type) {
      this.key = key;
      this.stopIndex = stopIndex;
      this.parentKey = parentKey;
      this.type = type;
    }

    @Override
    public int compareTo(Record o) {
      int c = this.type.compareTo(o.type);
      if (c != 0)
        return c;

      return (this.stopIndex - o.stopIndex);
    }
  }
}
