/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.model;

import java.text.DateFormat;

import org.onebusaway.transit_data_federation.impl.blocks.BlockSequence;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.InstanceState;
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

  private final InstanceState _state;

  private final int _frequencyOffset;

  private BlockSequence blockSequence;

  public StopTimeInstance(BlockStopTimeEntry stopTime, InstanceState state) {
    this(stopTime, state, UNSPECIFIED_FREQUENCY_OFFSET);
  }

  public StopTimeInstance(BlockStopTimeEntry stopTime, InstanceState state,
      int frequencyOffset) {
    _stopTime = stopTime;
    _state = state;
    _frequencyOffset = frequencyOffset;
  }

  public BlockStopTimeEntry getStopTime() {
    return _stopTime;
  }

  public InstanceState getState() {
    return _state;
  }

  public long getServiceDate() {
    return _state.getServiceDate();
  }

  public FrequencyEntry getFrequency() {
    return _state.getFrequency();
  }

  public FrequencyEntry getFrequencyLabel() {
    if (_state.getFrequency() != null)
      return _state.getFrequency();
    return _stopTime.getTrip().getTrip().getFrequencyLabel();
  }

  public boolean isFrequencyOffsetSpecified() {
    return isFrequencyOffsetSpecified(_frequencyOffset);
  }

  public int getFrequencyOffset() {
    return _frequencyOffset;
  }

  public BlockInstance getBlockInstance() {
    return new BlockInstance(_stopTime.getTrip().getBlockConfiguration(),
        _state);
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
    return _state.getServiceDate()
        + (_stopTime.getStopTime().getArrivalTime() + offset) * 1000;
  }

  public long getDepartureTime() {
    int offset = isFrequencyOffsetSpecified() ? _frequencyOffset : 0;
    return _state.getServiceDate()
        + (_stopTime.getStopTime().getDepartureTime() + offset) * 1000;
  }
  
  public StopTimeInstance getPreviousStopTimeInstance() {
    if (!_stopTime.hasPreviousStop())
      return null;
    /**
     * TODO: Check for frequency offset overflow?
     */
    return new StopTimeInstance(_stopTime.getPreviousStop(), _state,
        _frequencyOffset);
  }

  public StopTimeInstance getNextStopTimeInstance() {
    if (!_stopTime.hasNextStop())
      return null;
    /**
     * TODO: Check for frequency offset overflow?
     */
    return new StopTimeInstance(_stopTime.getNextStop(), _state,
        _frequencyOffset);
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
    result = prime * result + _frequencyOffset;
    result = prime * result + _state.hashCode();
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
    if (_frequencyOffset != other._frequencyOffset)
      return false;
    if (!_state.equals(other._state))
      return false;
    if (!_stopTime.equals(other._stopTime))
      return false;
    return true;
  }

  @Override
  public String toString() {

    long serviceDate = _state.getServiceDate();
    FrequencyEntry frequency = _state.getFrequency();

    if (frequency != null) {
      long start = serviceDate + frequency.getStartTime() * 1000;
      long end = serviceDate + frequency.getEndTime() * 1000;
      StringBuilder b = new StringBuilder();

      b.append("StopTimeInstance(stop=");
      b.append(_stopTime.getStopTime().getStop().getId());
      b.append(" trip=");
      b.append(getTrip());
      b.append(" service=");
      b.append(DAY_FORMAT.format(serviceDate));
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
          + " service=" + DAY_FORMAT.format(serviceDate) + " arrival="
          + TIME_FORMAT.format(getArrivalTime()) + " departure="
          + TIME_FORMAT.format(getDepartureTime()) + ")";
    }
  }

  public static boolean isFrequencyOffsetSpecified(int frequencyOffset) {
    return frequencyOffset != UNSPECIFIED_FREQUENCY_OFFSET;
  }
}
