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

import java.util.Arrays;

import org.onebusaway.collections.Range;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.ScheduledBlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.RealTimeHistoryService;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationHistogram;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationHistoryDao;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationSamples;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.InterpolationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.colt.list.DoubleArrayList;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import cern.jet.stat.Descriptive;

@Component
public class RealTimeHistoryServiceImpl implements RealTimeHistoryService {

  private ScheduleDeviationHistoryDao _scheduleDeviationHistoryDao;

  private int _predictionLookahead = 20 * 60;

  private Normal _schedDevScaleParam = new Normal(0, 5 * 60,
      RandomEngine.makeDefault());

  @Autowired
  public void setScheduleDeviationHistoryDao(
          ScheduleDeviationHistoryDao scheduleDeviationHistoryDao) {
    _scheduleDeviationHistoryDao = scheduleDeviationHistoryDao;
  }

  public void setPredictionLookahead(int predictionLookahead) {
    _predictionLookahead = predictionLookahead;
  }

  @Override
  public ScheduleDeviationHistogram getScheduleDeviationHistogramForArrivalAndDepartureInstance(
      ArrivalAndDepartureInstance instance, int stepSizeInSeconds) {

    BlockTripEntry blockTrip = instance.getBlockTrip();
    TripEntry trip = blockTrip.getTrip();
    AgencyAndId tripId = trip.getId();

    ScheduleDeviationHistory history = _scheduleDeviationHistoryDao.getScheduleDeviationHistoryForTripId(tripId);
    
    if( history == null)
      return null;

    BlockStopTimeEntry blockStopTime = instance.getBlockStopTime();
    StopTimeEntry stopTime = blockStopTime.getStopTime();

    double[] values = getScheduleDeviationsForScheduleTime(history,
        stopTime.getDepartureTime());
    return createHistogramFromValues(values, stepSizeInSeconds);
  }

  @Override
  public ScheduleDeviationSamples sampleScheduleDeviationsForVehicle(
      BlockInstance instance, VehicleLocationRecord record,
      ScheduledBlockLocation scheduledBlockLocation) {
    
	if (scheduledBlockLocation == null) 
	  return null;
    BlockTripEntry blockTrip = scheduledBlockLocation.getActiveTrip();
    TripEntry trip = blockTrip.getTrip();

    ScheduleDeviationHistory history = _scheduleDeviationHistoryDao.getScheduleDeviationHistoryForTripId(trip.getId());

    if (history == null)
      return null;

    ScheduleDeviationHistory resampledHistory = resampleHistory(history,
        scheduledBlockLocation.getScheduledTime(),
        record.getScheduleDeviation());

    DoubleArrayList scheduleTimes = new DoubleArrayList();
    DoubleArrayList mus = new DoubleArrayList();
    DoubleArrayList sigmas = new DoubleArrayList();

    for (int t = 0; t <= _predictionLookahead; t += 5 * 60) {

      int scheduleTime = scheduledBlockLocation.getScheduledTime() + t;
      double[] deviations = getScheduleDeviationsForScheduleTime(
          resampledHistory, scheduleTime);

      deviations = noNans(deviations);
      DoubleArrayList values = new DoubleArrayList(deviations);

      double mu = Descriptive.mean(values);
      double var = Descriptive.sampleVariance(values, mu);
      double sigma = Descriptive.sampleStandardDeviation(values.size(), var);

      scheduleTimes.add(scheduleTime);
      mus.add(mu);
      sigmas.add(sigma);
    }

    scheduleTimes.trimToSize();
    mus.trimToSize();
    sigmas.trimToSize();

    return new ScheduleDeviationSamples(scheduleTimes.elements(),
        mus.elements(), sigmas.elements());
  }

  /****
   * Private
   ****/

  /**
   * @param history
   * @param distanceAlongBlock
   * @return
   */

  private double[] getScheduleDeviationsForScheduleTime(
      ScheduleDeviationHistory history, int scheduleTime) {

    double[] scheduleTimes = history.getScheduleTimes();
    double[][] scheduleDeviations = history.getScheduleDeviations();

    int index = Arrays.binarySearch(scheduleTimes, scheduleTime);

    if (index >= 0)
      return getColumn(scheduleDeviations, index);

    index = -(index + 1);

    if (index == scheduleTimes.length)
      return getColumn(scheduleDeviations, index - 1);
    if (index == 0)
      return getColumn(scheduleDeviations, 0);

    int numSamples = history.getNumberOfSamples();
    double[] values = new double[numSamples];

    for (int i = 0; i < numSamples; i++) {
      double fromKey = scheduleTimes[index - 1];
      double toKey = scheduleTimes[index];
      double fromValue = scheduleDeviations[i][index - 1];
      double toValue = scheduleDeviations[i][index];
      values[i] = InterpolationLibrary.interpolatePair(fromKey, fromValue,
          toKey, toValue, scheduleTime);
    }

    return values;
  }

  private double[] getColumn(double[][] values, int index) {
    double[] v = new double[values.length];
    for (int i = 0; i < values.length; i++)
      v[i] = values[i][index];
    return v;
  }

  private double[] noNans(double[] values) {
    DoubleArrayList vs = new DoubleArrayList();
    for (double v : values) {
      if (!Double.isNaN(v))
        vs.add(v);
    }
    vs.trimToSize();
    return vs.elements();
  }

  private ScheduleDeviationHistogram createHistogramFromValues(double[] values,
      int stepSizeInSeconds) {

    values = noNans(values);

    if (values.length == 0)
      return new ScheduleDeviationHistogram(new int[0], new int[0]);

    Range r = new Range();
    for (double v : values)
      r.addValue(v);

    if (r.getRange() == 0)
      return new ScheduleDeviationHistogram(new int[] {(int) values[0]},
          new int[] {values.length});
    
    int halfStep = stepSizeInSeconds / 2;

    int from = (int) (Math.floor((r.getMin()-halfStep) / stepSizeInSeconds) * stepSizeInSeconds) + halfStep;
    int to = (int) (Math.ceil((r.getMax()+halfStep) / stepSizeInSeconds) * stepSizeInSeconds) - halfStep;
    int columns = (to - from) / stepSizeInSeconds;

    int[] scheduleDeviations = new int[columns];
    int[] counts = new int[columns];

    for (int i = 0; i < columns; i++)
      scheduleDeviations[i] = from + stepSizeInSeconds * i + halfStep;

    for (double value : values) {
      int index = (int) ((value - from) / stepSizeInSeconds);
      counts[index]++;
    }

    return new ScheduleDeviationHistogram(scheduleDeviations, counts);
  }

  private ScheduleDeviationHistory resampleHistory(
      ScheduleDeviationHistory history, int scheduleTime, double activeDeviation) {

    double[] deviations = getScheduleDeviationsForScheduleTime(history,
        scheduleTime);

    CDFMap<Integer> cdf = new CDFMap<Integer>();
    for (int i = 0; i < deviations.length; i++) {
      double deviation = deviations[i];
      if (Double.isNaN(deviation))
        continue;
      double p = _schedDevScaleParam.apply(activeDeviation - deviation);
      cdf.put(p, i);
    }

    int numSamples = deviations.length;

    double[] scheduleTimes = history.getScheduleTimes();
    double[][] scheduleDeviations = history.getScheduleDeviations();

    double[][] sampledScheduleDeviations = new double[numSamples][];

    for (int i = 0; i < numSamples; i++) {
      int index = cdf.sample();
      sampledScheduleDeviations[i] = scheduleDeviations[index];
    }

    return new ScheduleDeviationHistory(history.getTripId(), scheduleTimes,
        sampledScheduleDeviations);
  }
}
