package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Range;
import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.DelimiterTokenizerStrategy;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.AgencyAndIdInstance;
import org.onebusaway.transit_data_federation.impl.realtime.history.BlockLocationArchiveRecord;
import org.onebusaway.transit_data_federation.impl.realtime.history.ScheduleDeviationHistory;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationHistoryDao;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.utility.InterpolationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class BlockLocationHistoryTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(BlockLocationHistoryTask.class);

  private static final DistanceAlongBlockComparator _distanceAlongBlockComparator = new DistanceAlongBlockComparator();

  private TransitGraphDao _transitGraphDao;

  private ScheduleDeviationHistoryDao _scheduleDeviationHistoryDao;

  private File _path;

  private double _distanceAlongBlockStep = 500;

  private int _minSampleSize = 10;

  private double _outlierRatio = 2;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setScheduleDeviationHistoryDao(
      ScheduleDeviationHistoryDao scheduleDeviationHistoryDao) {
    _scheduleDeviationHistoryDao = scheduleDeviationHistoryDao;
  }

  public void setPath(File path) {
    _path = path;
  }

  public void setDistanceAlongBlockStep(double distanceAlongBlockStep) {
    _distanceAlongBlockStep = distanceAlongBlockStep;
  }

  public void seMinSampleSize(int minSampleSize) {
    _minSampleSize = minSampleSize;
  }

  public void setOutlierRatio(double outlierRatio) {
    _outlierRatio = outlierRatio;
  }

  @Override
  public void run() {

    if (_path == null) {
      _log.info("No BlockLocationHistoryTask data path specified.  Skipping this optional task");
    }

    if (!_path.exists()) {
      _log.warn("The specified BlockLocationHistoryTask data path does not exist!");
      return;
    }

    int blockIndex = 0;

    Iterable<BlockEntry> allBlocks = _transitGraphDao.getAllBlocks();

    for (BlockEntry block : allBlocks) {

      if (blockIndex % 100 == 0)
        _log.info("blocksProcessed=" + blockIndex);
      blockIndex++;

      List<File> files = getFilesForBlockId(block.getId());

      if (files.isEmpty())
        continue;

      Map<AgencyAndId, BlockLocationArchiveRecordMap> recordsByTrip = loadRecords(files);

      List<ScheduleDeviationHistory> histories = new ArrayList<ScheduleDeviationHistory>();

      for (Map.Entry<AgencyAndId, BlockLocationArchiveRecordMap> entry : recordsByTrip.entrySet()) {

        AgencyAndId tripId = entry.getKey();
        BlockLocationArchiveRecordMap recordsByInstance = entry.getValue();

        /**
         * If we don't have enough samples, skip the trip
         */
        if (recordsByInstance.size() < _minSampleSize)
          continue;

        ScheduleDeviationHistory history = constructHistory(tripId,
            recordsByInstance);
        histories.add(history);
      }

      if (!histories.isEmpty())
        _scheduleDeviationHistoryDao.saveScheduleDeviationHistory(histories);
    }
  }

  private List<File> getFilesForBlockId(AgencyAndId blockId) {

    List<File> files = new ArrayList<File>();
    for (File dateDir : _path.listFiles()) {
      File dataFile = new File(dateDir,
          AgencyAndIdLibrary.convertToString(blockId) + ".gz");
      if (dataFile.exists())
        files.add(dataFile);
    }
    return files;
  }

  private Map<AgencyAndId, BlockLocationArchiveRecordMap> loadRecords(
      List<File> files) {

    CsvEntityReader reader = new CsvEntityReader();
    reader.setTokenizerStrategy(new DelimiterTokenizerStrategy("\t"));

    EntityHandlerImpl handler = new EntityHandlerImpl();
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

    return handler.getRecordsByTrip();
  }

  private InputStream openFileForInput(File path) throws IOException {
    InputStream in = new FileInputStream(path);
    if (path.getName().endsWith(".gz"))
      in = new GZIPInputStream(in);
    return in;
  }

  private ScheduleDeviationHistory constructHistory(AgencyAndId tripId,
      BlockLocationArchiveRecordMap recordsByInstance) {

    List<SortedMap<Double, Double>> traces = new ArrayList<SortedMap<Double, Double>>();
    Range xRange = new Range();

    sortAndArrangeTraces(recordsByInstance, traces, xRange);

    double step = computeSamplingStep(traces);

    double from = Math.ceil(xRange.getMin() / step)
        * step;
    double to = Math.floor(xRange.getMax() / step)
        * step;

    SortedMap<Double, Double> mus = new TreeMap<Double, Double>();
    SortedMap<Double, Double> sigmas = new TreeMap<Double, Double>();

    computeMeanAndStandardDeviationForTraces(traces, from, to, step, mus, sigmas);

    removeOutlierTraces(traces, mus, sigmas);

    int numOfTraces = traces.size();

    DoubleArrayList distancesAlongBlock = new DoubleArrayList();
    List<DoubleArrayList> scheduleDeviations = new ArrayList<DoubleArrayList>();

    for (int i = 0; i < numOfTraces; i++)
      scheduleDeviations.add(new DoubleArrayList());

    for (double x = from; x <= to; x += step) {

      DoubleArrayList rawValues = new DoubleArrayList();
      DoubleArrayList values = new DoubleArrayList();

      for (SortedMap<Double, Double> m : traces) {
        if (x < m.firstKey() || x > m.lastKey()) {
          rawValues.add(Double.NaN);
          continue;
        }
        double schedDev = InterpolationLibrary.interpolate(m, x);
        values.add(schedDev);
        rawValues.add(schedDev);
      }

      if (values.size() < Math.max(_minSampleSize, 2))
        continue;

      double mu = Descriptive.mean(values);
      double sigma = Descriptive.sampleStandardDeviation(values.size(),
          Descriptive.sampleVariance(values, mu));

      int goodValueCount = pruneOutlierValues(rawValues, mu, sigma);
      if (goodValueCount < _minSampleSize)
        continue;

      distancesAlongBlock.add(x);
      for (int traceIndex = 0; traceIndex < traces.size(); traceIndex++)
        scheduleDeviations.get(traceIndex).add(rawValues.get(traceIndex));
    }

    distancesAlongBlock.trimToSize();
    double[] distanceAlongBlockArray = distancesAlongBlock.elements();

    int numOfValues = distancesAlongBlock.size();

    double[][] scheduleDeviationsArrays = new double[numOfValues][numOfTraces];

    for (int valueIndex = 0; valueIndex < numOfValues; valueIndex++) {
      for (int traceIndex = 0; traceIndex < numOfTraces; traceIndex++) {
        scheduleDeviationsArrays[valueIndex][traceIndex] = scheduleDeviations.get(
            traceIndex).get(valueIndex);
      }
    }

    return new ScheduleDeviationHistory(tripId, distanceAlongBlockArray,
        scheduleDeviationsArrays);
  }

  private void sortAndArrangeTraces(
      BlockLocationArchiveRecordMap recordsByInstance,
      List<SortedMap<Double, Double>> maps, Range xRange) {

    for (List<BlockLocationArchiveRecord> records : recordsByInstance.values()) {

      Collections.sort(records, _distanceAlongBlockComparator);

      SortedMap<Double, Double> m = new TreeMap<Double, Double>();
      for (BlockLocationArchiveRecord record : records) {
        m.put(record.getDistanceAlongBlock(),
            (double) record.getScheduleDeviation());
        xRange.addValue(record.getDistanceAlongBlock());
      }
      maps.add(m);
    }
  }

  private double computeSamplingStep(List<SortedMap<Double, Double>> traces) {

    double minStep = Double.POSITIVE_INFINITY;

    for (SortedMap<Double, Double> m : traces) {
      if (m.size() < 5)
        continue;
      double step = (m.lastKey() - m.firstKey()) / m.size();
      minStep = Math.min(step,minStep);
    }
    
    if( Double.isInfinite(minStep))
      return _distanceAlongBlockStep;
    
    return Math.ceil(minStep / 100) * 100;
  }

  private void computeMeanAndStandardDeviationForTraces(
      List<SortedMap<Double, Double>> traces, double from, double to,
      double step, SortedMap<Double, Double> mus, SortedMap<Double, Double> sigmas) {

    for (double x = from; x <= to; x += step) {
      DoubleArrayList values = new DoubleArrayList();
      for (SortedMap<Double, Double> m : traces) {
        if (x < m.firstKey() || x > m.lastKey())
          continue;
        double schedDev = InterpolationLibrary.interpolate(m, x);
        values.add(schedDev);
      }
      if (values.size() < 2)
        continue;
      double mu = Descriptive.mean(values);
      double sigma = Descriptive.sampleStandardDeviation(values.size(),
          Descriptive.sampleVariance(values, mu));
      mus.put(x, mu);
      sigmas.put(x, sigma);
    }
  }

  private void removeOutlierTraces(List<SortedMap<Double, Double>> maps,
      SortedMap<Double, Double> mus, SortedMap<Double, Double> sigmas) {

    Iterator<SortedMap<Double, Double>> it = maps.iterator();
    while (it.hasNext()) {
      SortedMap<Double, Double> m = it.next();
      if (isTraceAnOutlier(m, mus, sigmas))
        it.remove();
    }
  }

  private boolean isTraceAnOutlier(SortedMap<Double, Double> m,
      SortedMap<Double, Double> mus, SortedMap<Double, Double> sigmas) {

    double outliers = 0;

    for (Map.Entry<Double, Double> entry : m.entrySet()) {
      double x = entry.getKey();
      double value = entry.getValue();
      double mu = InterpolationLibrary.interpolate(mus, x);
      double sigma = InterpolationLibrary.interpolate(sigmas, x);
      if (Math.abs(value - mu) > _outlierRatio * sigma)
        outliers++;
    }

    /**
     * Is more than half the data an outlier? Then exclude the trace
     */
    return (outliers / m.size() > 0.5);
  }

  private int pruneOutlierValues(DoubleArrayList rawValues, double mu,
      double sigma) {
    int goodValueCount = 0;
    for (int i = 0; i < rawValues.size(); i++) {
      double value = rawValues.get(i);
      if (Double.isNaN(value))
        continue;
      if (Math.abs(value - mu) > _outlierRatio * sigma)
        rawValues.set(i, Double.NaN);
      else
        goodValueCount++;
    }
    return goodValueCount;
  }

  /****
   * 
   ****/

  private class EntityHandlerImpl implements EntityHandler {

    private Map<AgencyAndId, BlockLocationArchiveRecordMap> _recordsByTrip = new FactoryMap<AgencyAndId, BlockLocationArchiveRecordMap>(
        new BlockLocationArchiveRecordMap());

    public Map<AgencyAndId, BlockLocationArchiveRecordMap> getRecordsByTrip() {
      return _recordsByTrip;
    }

    @Override
    public void handleEntity(Object bean) {
      BlockLocationArchiveRecord record = (BlockLocationArchiveRecord) bean;
      AgencyAndId tripId = record.getTripId();
      AgencyAndIdInstance instance = new AgencyAndIdInstance(tripId,
          record.getServiceDate());
      _recordsByTrip.get(tripId).get(instance).add(record);
    }
  }

  public static class BlockLocationArchiveRecordMap extends
      FactoryMap<AgencyAndIdInstance, List<BlockLocationArchiveRecord>> {

    private static final long serialVersionUID = 1L;

    public BlockLocationArchiveRecordMap() {
      super(new ArrayList<BlockLocationArchiveRecord>());
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
