package org.onebusaway.transit_data_federation.services.tripplanner;

import java.text.DateFormat;
import java.util.Date;

import org.onebusaway.transit_data_federation.impl.blocks.BlockSequence;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;

public class StopTimeInstance {

  private static final DateFormat DAY_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

  private static final DateFormat TIME_FORMAT = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT);

  public static final int UNSPECIFIED_FREQUENCY_OFFSET = Integer.MIN_VALUE;

  private final BlockStopTimeEntry _stopTime;

  private final long _serviceDate;

  private final FrequencyEntry _frequency;

  private final int _frequencyOffset;

  private BlockSequence blockSequence;

  public StopTimeInstance(BlockStopTimeEntry stopTime, Date serviceDate) {
    this(stopTime, serviceDate.getTime());
  }

  public StopTimeInstance(BlockStopTimeEntry stopTime, long serviceDate) {
    this(stopTime, serviceDate, null, UNSPECIFIED_FREQUENCY_OFFSET);
  }

  public StopTimeInstance(BlockStopTimeEntry stopTime, long serviceDate,
      FrequencyEntry frequency) {
    this(stopTime, serviceDate, frequency, UNSPECIFIED_FREQUENCY_OFFSET);
  }

  public StopTimeInstance(BlockStopTimeEntry stopTime, long serviceDate,
      FrequencyEntry frequency, int frequencyOffset) {
    _stopTime = stopTime;
    _serviceDate = serviceDate;
    _frequency = frequency;
    _frequencyOffset = frequencyOffset;
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

  public boolean isFrequencyOffsetSpecified() {
    return isFrequencyOffsetSpecified(_frequencyOffset);
  }

  public int getFrequencyOffset() {
    return _frequencyOffset;
  }

  public BlockInstance getBlockInstance() {
    return new BlockInstance(_stopTime.getTrip().getBlockConfiguration(),
        _serviceDate, _frequency);
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
    int offset = isFrequencyOffsetSpecified() ? _frequencyOffset : 0;
    return _serviceDate + (_stopTime.getStopTime().getArrivalTime() + offset)
        * 1000;
  }

  public long getDepartureTime() {
    int offset = isFrequencyOffsetSpecified() ? _frequencyOffset : 0;
    return _serviceDate + (_stopTime.getStopTime().getDepartureTime() + offset)
        * 1000;
  }

  public StopTimeInstance getNextStopTimeInstance() {
    if (!_stopTime.hasNextStop())
      return null;
    /**
     * TODO: Check for frequency offset overflow?
     */
    return new StopTimeInstance(_stopTime.getNextStop(), _serviceDate,
        _frequency, _frequencyOffset);
  }

  public BlockSequence getBlockSequence() {
    return blockSequence;
  }

  public void setBlockSequence(BlockSequence blockSequence) {
    this.blockSequence = blockSequence;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_frequency == null) ? 0 : _frequency.hashCode());
    result = prime * result + _frequencyOffset;
    result = prime * result + (int) (_serviceDate ^ (_serviceDate >>> 32));
    result = prime * result + _stopTime.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StopTimeInstance other = (StopTimeInstance) obj;
    if (_frequency == null) {
      if (other._frequency != null)
        return false;
    } else if (!_frequency.equals(other._frequency))
      return false;
    if (_frequencyOffset != other._frequencyOffset)
      return false;
    if (_serviceDate != other._serviceDate)
      return false;
    if (!_stopTime.equals(other._stopTime))
      return false;
    return true;
  }

  @Override
  public String toString() {

    if (_frequency != null) {

      long start = _serviceDate + _frequency.getStartTime() * 1000;
      long end = _serviceDate + _frequency.getEndTime() * 1000;
      StringBuilder b = new StringBuilder();

      b.append("StopTimeInstance(stop=");
      b.append(_stopTime.getStopTime().getStop().getId());
      b.append(" trip=");
      b.append(getTrip());
      b.append(" service=");
      b.append(DAY_FORMAT.format(_serviceDate));
      b.append(" start=");
      b.append(TIME_FORMAT.format(start));
      b.append(" end=");
      b.append(TIME_FORMAT.format(end));
      if (isFrequencyOffsetSpecified()) {
        b.append(" arrival=");
        b.append(TIME_FORMAT.format(getArrivalTime()));
        b.append(" departure=");
        b.append(TIME_FORMAT.format(getDepartureTime()));
      }
      b.append(")");
      return b.toString();
    } else {
      return "StopTimeInstance(stop="
          + _stopTime.getStopTime().getStop().getId() + " trip=" + getTrip()
          + " service=" + DAY_FORMAT.format(_serviceDate) + " arrival="
          + TIME_FORMAT.format(getArrivalTime()) + " departure="
          + TIME_FORMAT.format(getDepartureTime()) + ")";
    }
  }

  public static boolean isFrequencyOffsetSpecified(int frequencyOffset) {
    return frequencyOffset != UNSPECIFIED_FREQUENCY_OFFSET;
  }
}
