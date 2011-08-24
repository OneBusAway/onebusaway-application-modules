package org.onebusaway.transit_data_federation.services.transit_graph;

import java.text.DateFormat;
import java.util.Date;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class StopTimeInstance {

  private static final DateFormat DAY_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

  private static final DateFormat TIME_FORMAT = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT);

  private final BlockStopTimeEntry _stopTime;

  private final long _serviceDate;

  private final FrequencyEntry _frequency;

  public StopTimeInstance(BlockStopTimeEntry stopTime, Date serviceDate) {
    this(stopTime, serviceDate.getTime());
  }

  public StopTimeInstance(BlockStopTimeEntry stopTime, long serviceDate) {
    this(stopTime, serviceDate, null);
  }

  public StopTimeInstance(BlockStopTimeEntry stopTime, long serviceDate,
      FrequencyEntry frequency) {
    _stopTime = stopTime;
    _serviceDate = serviceDate;
    _frequency = frequency;
  }

  public BlockStopTimeEntry getStopTime() {
    return _stopTime;
  }

  public long getServiceDate() {
    return _serviceDate;
  }

  public FrequencyEntry getFrequency() {
    return _frequency;
  }

  public BlockTripEntry getTrip() {
    return _stopTime.getTrip();
  }

  public int getSequence() {
    return _stopTime.getBlockSequence();
  }

  public StopEntry getStop() {
    return _stopTime.getStopTime().getStop();
  }

  public long getArrivalTime() {
    return _serviceDate + _stopTime.getStopTime().getArrivalTime() * 1000;
  }

  public long getDepartureTime() {
    return _serviceDate + _stopTime.getStopTime().getDepartureTime() * 1000;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof StopTimeInstance))
      return false;
    StopTimeInstance other = (StopTimeInstance) obj;
    return _stopTime.equals(other._stopTime)
        && _serviceDate == other._serviceDate;
  }

  @Override
  public int hashCode() {
    return _stopTime.hashCode() + new Long(_serviceDate).hashCode();
  }

  @Override
  public String toString() {
    return "StopTimeInstance(stop=" + _stopTime.getStopTime().getStop().getId()
        + " trip=" + getTrip() + " service=" + DAY_FORMAT.format(_serviceDate)
        + " arrival=" + TIME_FORMAT.format(getArrivalTime()) + " departure="
        + TIME_FORMAT.format(getDepartureTime()) + ")";
  }
}
