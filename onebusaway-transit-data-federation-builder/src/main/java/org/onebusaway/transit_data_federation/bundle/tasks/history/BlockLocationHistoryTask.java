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
package org.onebusaway.transit_data_federation.bundle.tasks.history;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.Range;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.AgencyAndIdInstance;
import org.onebusaway.transit_data_federation.impl.realtime.history.BlockLocationArchiveRecord;
import org.onebusaway.transit_data_federation.impl.realtime.history.ScheduleDeviationHistory;
import org.onebusaway.util.AgencyAndIdLibrary;
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

  private BlockLocationArchiveSource _source;

  private int _sampleTimeStep = 300;

  private int _minSampleSize = 10;

  private double _outlierRatio = 2;

  private AgencyAndId _skipToTrip = null;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setScheduleDeviationHistoryDao(
      ScheduleDeviationHistoryDao scheduleDeviationHistoryDao) {
    _scheduleDeviationHistoryDao = scheduleDeviationHistoryDao;
  }

  public void setSource(BlockLocationArchiveSource source) {
    _source = source;
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

  public void setSkipToTrip(String tripId) {
    _skipToTrip = AgencyAndIdLibrary.convertFromString(tripId);
  }

  @Override
  public void run() {

    if (_source == null) {
      _log.info("No BlockLocationHistoryTask data source specified.  Skipping this optional task");
    }

    int tripIndex = 0;

    Iterable<TripEntry> allTrips = _transitGraphDao.getAllTrips();

    boolean skipTo = _skipToTrip != null;

    for (TripEntry trip : allTrips) {

      if (tripIndex % 20 == 0)
        _log.info("tripsProcessed=" + tripIndex);
      tripIndex++;

      if (_skipToTrip != null && trip.getId().equals(_skipToTrip)) {
        skipTo = false;
      } else if (!skipTo) {
        try {
          processTrip(trip);
        } catch (Throwable ex) {
          _log.warn("error processing trip " + trip.getId(), ex);
        }
      }
    }
  }

  private void processTrip(TripEntry trip) {

    List<BlockLocationArchiveRecord> records = _source.getRecordsForTrip(trip.getId());

    Map<AgencyAndId, BlockLocationArchiveRecordMap> recordsByTrip = loadRecords(records);

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

  private Map<AgencyAndId, BlockLocationArchiveRecordMap> loadRecords(
      List<BlockLocationArchiveRecord> records) {

    Map<AgencyAndId, BlockLocationArchiveRecordMap> recordsByTrip = new FactoryMap<AgencyAndId, BlockLocationArchiveRecordMap>(
        new BlockLocationArchiveRecordMap());

    for (BlockLocationArchiveRecord record : records) {
      AgencyAndId tripId = record.getTripId();
      AgencyAndIdInstance instance = new AgencyAndIdInstance(tripId,
          record.getServiceDate());
      recordsByTrip.get(record.getTripId()).get(instance).add(record);
    }

    return recordsByTrip;
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
      if (step == 0)
        continue;
      minStep = Math.min(step, minStep);
    }

    if (minStep == Integer.MAX_VALUE || minStep == 0)
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

  public static class BlockLocationArchiveRecordMap extends
      FactoryMap<AgencyAndIdInstance, List<BlockLocationArchiveRecord>> {

    private static final long serialVersionUID = 1L;

    public BlockLocationArchiveRecordMap() {
      super(new ArrayList<BlockLocationArchiveRecord>());
    }
  }
}
