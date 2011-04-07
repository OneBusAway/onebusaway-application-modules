package org.onebusaway.transit_data_federation.impl.realtime.history;

import java.util.Arrays;

import org.onebusaway.collections.Range;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.RealTimeHistoryService;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationHistogram;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.utility.InterpolationLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.colt.list.DoubleArrayList;

@Component
public class RealTimeHistoryServiceImpl implements RealTimeHistoryService {

  private ScheduleDeviationHistoryDaoImpl _scheduleDeviationHistoryDao;

  @Autowired
  public void setScheduleDeviationHistoryDao(
      ScheduleDeviationHistoryDaoImpl scheduleDeviationHistoryDao) {
    _scheduleDeviationHistoryDao = scheduleDeviationHistoryDao;
  }

  @Override
  public ScheduleDeviationHistogram getScheduleDeviationHistogramForArrivalAndDepartureInstance(
      ArrivalAndDepartureInstance instance, int stepSizeInSeconds) {

    BlockTripEntry blockTrip = instance.getBlockTrip();
    TripEntry trip = blockTrip.getTrip();
    AgencyAndId tripId = trip.getId();

    ScheduleDeviationHistory history = _scheduleDeviationHistoryDao.getScheduleDeviationHistoryForTripId(tripId);

    BlockStopTimeEntry blockStopTime = instance.getBlockStopTime();
    double distanceAlongBlock = blockStopTime.getDistanceAlongBlock();

    double[] values = getScheduleDeviationsForDistanceAlongBlock(history,
        distanceAlongBlock);
    return createHistogramFromValues(values, stepSizeInSeconds);
  }

  private double[] getScheduleDeviationsForDistanceAlongBlock(
      ScheduleDeviationHistory history, double distanceAlongBlock) {

    double[] distancesAlongBlock = history.getDistancesAlongBlock();
    double[][] scheduleDeviations = history.getScheduleDeviations();

    int index = Arrays.binarySearch(distancesAlongBlock, distanceAlongBlock);

    if (index >= 0)
      return scheduleDeviations[index];

    index = -(index + 1);

    if (index == distancesAlongBlock.length)
      return noNans(scheduleDeviations[index - 1]);
    if (index == 0)
      return noNans(scheduleDeviations[0]);

    int numSamples = history.getNumberOfSamples();
    DoubleArrayList values = new DoubleArrayList();

    for (int i = 0; i < numSamples; i++) {
      double fromKey = distancesAlongBlock[index - 1];
      double toKey = distancesAlongBlock[index];
      double fromValue = scheduleDeviations[index - 1][i];
      double toValue = scheduleDeviations[index][i];
      double v = InterpolationLibrary.interpolatePair(fromKey, fromValue,
          toKey, toValue, distanceAlongBlock);
      if (!Double.isNaN(v))
        values.add(v);
    }

    values.trimToSize();
    return values.elements();
  }
  
  private double[] noNans(double[] values) {
    DoubleArrayList vs = new DoubleArrayList();
    for(double v : values) {
      if( ! Double.isNaN(v))
        vs.add(v);
    }
    vs.trimToSize();
    return vs.elements();
  }

  private ScheduleDeviationHistogram createHistogramFromValues(double[] values,
      int stepSizeInSeconds) {

    if (values.length == 0)
      return new ScheduleDeviationHistogram(new int[0], new int[0]);

    Range r = new Range();
    for (double v : values)
      r.addValue(v);

    if (r.getRange() == 0)
      return new ScheduleDeviationHistogram(new int[] {(int) values[0]},
          new int[] {values.length});

    int from = (int) (Math.floor(r.getMin() / stepSizeInSeconds) * stepSizeInSeconds);
    int to = (int) (Math.ceil(r.getMax() / stepSizeInSeconds) * stepSizeInSeconds);
    int columns = (to - from) / stepSizeInSeconds + 1;

    int[] scheduleDeviations = new int[columns];
    int[] counts = new int[columns];

    for (int i = 0; i < columns; i++)
      scheduleDeviations[i] = from + stepSizeInSeconds * i;

    for (double value : values) {
      int index = (int) ((value - from) / stepSizeInSeconds);
      counts[index]++;
    }

    return new ScheduleDeviationHistogram(scheduleDeviations, counts);
  }
}
