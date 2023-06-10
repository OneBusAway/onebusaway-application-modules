/**
 * Copyright (C) 2023 Cambridge Systematics, Inc.
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
package org.onebusaway.transit_data_federation.services.transit_graph.dynamic;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;

public class DynamicBlockStopTimeEntryImpl implements BlockStopTimeEntry {

    private StopTimeEntry stopTime;

    private BlockTripEntry trip;

    private int blockSequence;

    private boolean hasNextStop;

    public DynamicBlockStopTimeEntryImpl(StopTimeEntry stopTime, int blockSequence,
                                  BlockTripEntry trip, boolean hasNextStop) {

        if (stopTime == null)
            throw new IllegalArgumentException("stopTime is null");
        if (trip == null)
            throw new IllegalArgumentException("trip is null");

        this.stopTime = stopTime;
        this.trip = trip;
        this.blockSequence = blockSequence;
        this.hasNextStop = hasNextStop;
    }

    public void setStopTime(StopTimeEntry stopTime) {
        this.stopTime = stopTime;
    }

    public void setTrip(BlockTripEntry trip) {
        this.trip = trip;
    }

    public void setBlockSequence(int blockSequence) {
        this.blockSequence = blockSequence;
    }

    public void setHasNextStop(boolean hasNextStop) {
        this.hasNextStop = hasNextStop;
    }

    @Override
    public StopTimeEntry getStopTime() {
        return stopTime;
    }

    @Override
    public BlockTripEntry getTrip() {
        return trip;
    }

    @Override
    public int getBlockSequence() {
        return blockSequence;
    }

    @Override
    public double getDistanceAlongBlock() {
        return trip.getDistanceAlongBlock() + stopTime.getShapeDistTraveled();
    }

    @Override
    public int getAccumulatedSlackTime() {
        return trip.getAccumulatedSlackTime() + stopTime.getAccumulatedSlackTime();
    }

    @Override
    public boolean hasPreviousStop() {
        return blockSequence > 0;
    }

    @Override
    public boolean hasNextStop() {
        return hasNextStop;
    }

    @Override
    public BlockStopTimeEntry getPreviousStop() {
        return trip.getBlockConfiguration().getStopTimes().get(blockSequence);
    }

    @Override
    public BlockStopTimeEntry getNextStop() {
        return trip.getBlockConfiguration().getStopTimes().get(blockSequence + 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + blockSequence;
        result = prime * result + stopTime.hashCode();
        result = prime * result + trip.hashCode();
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
        DynamicBlockStopTimeEntryImpl other = (DynamicBlockStopTimeEntryImpl) obj;
        if (blockSequence != other.blockSequence)
            return false;
        if (!stopTime.equals(other.stopTime))
            return false;
        if (!trip.equals(other.trip))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BlockStopTime(stopTime=" + stopTime + " blockSeq=" + blockSequence
                + " trip=" + trip + ")";
    }

}
