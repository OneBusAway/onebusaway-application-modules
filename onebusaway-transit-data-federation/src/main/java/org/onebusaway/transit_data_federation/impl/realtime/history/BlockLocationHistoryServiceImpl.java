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
package org.onebusaway.transit_data_federation.impl.realtime.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Range;
import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.DelimiterTokenizerStrategy;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.AgencyAndIdInstance;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationHistoryService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BlockLocationHistoryServiceImpl implements
    BlockLocationHistoryService {

  private TransitGraphDao _transitGraphDao;

  private File _dataDir;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  public void setDataDir(File dataDir) {
    _dataDir = dataDir;
  }

  @Override
  public Map<AgencyAndIdInstance, List<BlockLocationArchiveRecord>> getHistoryForTripId(AgencyAndId tripId) {

    TripEntry trip = _transitGraphDao.getTripEntryForId(tripId);

    if (trip == null)
      return null;

    BlockEntry block = trip.getBlock();

    List<File> files = getFilesForBlockId(block.getId());

    CsvEntityReader reader = new CsvEntityReader();
    reader.setTokenizerStrategy(new DelimiterTokenizerStrategy("\t"));

    EntityHandlerImpl handler = new EntityHandlerImpl(tripId);
    reader.addEntityHandler(handler);

    try {
      for (File file : files) {
        InputStream in = openFileForInput(file);
        reader.readEntities(BlockLocationArchiveRecord.class, in);
        in.close();
      }
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }

    Map<AgencyAndIdInstance, List<BlockLocationArchiveRecord>> recordsByInstance = handler.getRecordsByInstance();

    for (List<BlockLocationArchiveRecord> records : recordsByInstance.values())
      Collections.sort(records, new DistanceAlongBlockComparator());
    
    return recordsByInstance;
  }

  private InputStream openFileForInput(File path) throws IOException {
    InputStream in = new FileInputStream(path);
    if (path.getName().endsWith(".gz"))
      in = new GZIPInputStream(in);
    return in;
  }

  private List<File> getFilesForBlockId(AgencyAndId blockId) {

    List<File> files = new ArrayList<File>();
    for (File dateDir : _dataDir.listFiles()) {
      File dataFile = new File(dateDir,
          AgencyAndIdLibrary.convertToString(blockId) + ".gz");
      if (dataFile.exists())
        files.add(dataFile);
    }
    return files;
  }

  private class EntityHandlerImpl implements EntityHandler {

    private Range _distanceAlongBlockRange = new Range();

    private Range _scheduleDeviationRange = new Range();

    private Map<AgencyAndIdInstance, List<BlockLocationArchiveRecord>> _recordsByInstance = new FactoryMap<AgencyAndIdInstance, List<BlockLocationArchiveRecord>>(
        new ArrayList<BlockLocationArchiveRecord>());

    private final AgencyAndId _tripId;

    public EntityHandlerImpl(AgencyAndId tripId) {
      _tripId = tripId;
    }

    public Range getDistanceAlongBlockRange() {
      return _distanceAlongBlockRange;
    }

    public Range getScheduleDeviationRange() {
      return _scheduleDeviationRange;
    }

    public Map<AgencyAndIdInstance, List<BlockLocationArchiveRecord>> getRecordsByInstance() {
      return _recordsByInstance;
    }

    @Override
    public void handleEntity(Object bean) {
      BlockLocationArchiveRecord record = (BlockLocationArchiveRecord) bean;
      if (!_tripId.equals(record.getTripId()))
        return;
      AgencyAndIdInstance instance = new AgencyAndIdInstance(
          record.getTripId(), record.getServiceDate());
      _recordsByInstance.get(instance).add(record);
      _distanceAlongBlockRange.addValue(record.getDistanceAlongBlock());
      _scheduleDeviationRange.addValue(record.getScheduleDeviation());
    }
  }

  private static class DistanceAlongBlockComparator implements
      Comparator<BlockLocationArchiveRecord> {

    @Override
    public int compare(BlockLocationArchiveRecord o1,
        BlockLocationArchiveRecord o2) {
      return Double.compare(o1.getDistanceAlongBlock(),
          o2.getDistanceAlongBlock());
    }
  }
}
