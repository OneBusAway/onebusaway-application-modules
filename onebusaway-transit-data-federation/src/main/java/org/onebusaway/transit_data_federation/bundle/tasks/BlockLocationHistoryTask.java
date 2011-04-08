package org.onebusaway.transit_data_federation.bundle.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.InterpolationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

public class BlockLocationHistoryTask implements Runnable {

  private static Logger _log = LoggerFactory.getLogger(BlockLocationHistoryTask.class);

  private TransitGraphDao _transitGraphDao;

  private ScheduleDeviationHistoryDao _scheduleDeviationHistoryDao;

  private File _path;

  private int _sampleTimeStep = 300;

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

  public void setSampleStepSize(int sampleTimeStep) {
    _sampleTimeStep = sampleTimeStep;
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

    int tripIndex = 0;

    Iterable<TripEntry> allTrips = _transitGraphDao.getAllTrips();

    for (TripEntry trip : allTrips) {

      if (tripIndex % 100 == 0)
        _log.info("tripsProcessed=" + tripIndex);
      tripIndex++;

      try {
        processTrip(trip);
      } catch (Throwable ex) {
        _log.warn("error processing trip " + trip.getId(), ex);
      }
    }
  }

  private void processTrip(TripEntry trip) {

    List<File> files = getFilesForTripId(trip.getId());

    if (files.isEmpty())
      return;

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

  private List<File> getFilesForTripId(AgencyAndId tripId) {

    String id = AgencyAndIdLibrary.convertToString(tripId);
    int n = id.length();
    String key = id.substring(n - 2, n);

    List<File> files = new ArrayList<File>();
    for (File dateDir : _path.listFiles()) {
      File dataFile = new File(dateDir, key + File.separator + "trip-" + id
          + ".gz");
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

    List<SortedMap<Integer, Double>> traces = new ArrayList<SortedMap<Integer, Double>>();
    Range tRange = new Range();

    sortAndArrangeTraces(recordsByInstance, traces, tRange);

    int step = computeSamplingStep(traces);

    int from = (int) (Math.ceil(tRange.getMin() / step) * step);
    int to = (int) (Math.floor(tRange.getMax() / step) * step);

    SortedMap<Integer, Double> mus = new TreeMap<Integer, Double>();
    SortedMap<Integer, Double> sigmas = new TreeMap<Integer, Double>();

    computeMeanAndStandardDeviationForTraces(traces, from, to, step, mus,
        sigmas);

    removeOutlierTraces(traces, mus, sigmas);

    int numOfTraces = traces.size();

    DoubleArrayList scheduleTimes = new DoubleArrayList();
    List<DoubleArrayList> scheduleDeviations = new ArrayList<DoubleArrayList>();

    for (int i = 0; i < numOfTraces; i++)
      scheduleDeviations.add(new DoubleArrayList());

    for (int t = from; t <= to; t += step) {

      DoubleArrayList rawValues = new DoubleArrayList();
      DoubleArrayList values = new DoubleArrayList();

      for (SortedMap<Integer, Double> m : traces) {
        if (t < m.firstKey() || t > m.lastKey()) {
          rawValues.add(Double.NaN);
          continue;
        }
        double schedDev = InterpolationLibrary.interpolate(m, t);
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

      scheduleTimes.add(t);
      for (int traceIndex = 0; traceIndex < traces.size(); traceIndex++)
        scheduleDeviations.get(traceIndex).add(rawValues.get(traceIndex));
    }

    scheduleTimes.trimToSize();
    double[] scheduleTimesArray = scheduleTimes.elements();

    double[][] scheduleDeviationsArrays = new double[numOfTraces][];

    for (int traceIndex = 0; traceIndex < numOfTraces; traceIndex++) {
      DoubleArrayList list = scheduleDeviations.get(traceIndex);
      list.trimToSize();
      scheduleDeviationsArrays[traceIndex] = list.elements();
    }

    return new ScheduleDeviationHistory(tripId, scheduleTimesArray,
        scheduleDeviationsArrays);
  }

  private void sortAndArrangeTraces(
      BlockLocationArchiveRecordMap recordsByInstance,
      List<SortedMap<Integer, Double>> maps, Range tRange) {

    for (List<BlockLocationArchiveRecord> records : recordsByInstance.values()) {

      SortedMap<Integer, Double> m = new TreeMap<Integer, Double>();
      for (BlockLocationArchiveRecord record : records) {
        int effectiveScheduleTime = (int) ((record.getTime() - record.getServiceDate()) / 1000 - record.getScheduleDeviation());
        m.put(effectiveScheduleTime, (double) record.getScheduleDeviation());
        tRange.addValue(effectiveScheduleTime);
      }
      maps.add(m);
    }
  }

  private int computeSamplingStep(List<SortedMap<Integer, Double>> traces) {

    int minStep = Integer.MAX_VALUE;

    for (SortedMap<Integer, Double> m : traces) {
      if (m.size() < 5)
        continue;
      int step = (m.lastKey() - m.firstKey()) / m.size();
      minStep = Math.min(step, minStep);
    }

    if (minStep == Integer.MAX_VALUE)
      return _sampleTimeStep;

    return (int) (Math.ceil(minStep / 60.0) * 60);
  }

  private void computeMeanAndStandardDeviationForTraces(
      List<SortedMap<Integer, Double>> traces, int from, int to, int step,
      SortedMap<Integer, Double> mus, SortedMap<Integer, Double> sigmas) {

    for (int x = from; x <= to; x += step) {
      DoubleArrayList values = new DoubleArrayList();
      for (SortedMap<Integer, Double> m : traces) {
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

  private void removeOutlierTraces(List<SortedMap<Integer, Double>> maps,
      SortedMap<Integer, Double> mus, SortedMap<Integer, Double> sigmas) {

    Iterator<SortedMap<Integer, Double>> it = maps.iterator();
    while (it.hasNext()) {
      SortedMap<Integer, Double> m = it.next();
      if (isTraceAnOutlier(m, mus, sigmas))
        it.remove();
    }
  }

  private boolean isTraceAnOutlier(SortedMap<Integer, Double> m,
      SortedMap<Integer, Double> mus, SortedMap<Integer, Double> sigmas) {

    double outliers = 0;

    for (Map.Entry<Integer, Double> entry : m.entrySet()) {
      int t = entry.getKey();
      double value = entry.getValue();
      double mu = InterpolationLibrary.interpolate(mus, t);
      double sigma = InterpolationLibrary.interpolate(sigmas, t);
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
}
