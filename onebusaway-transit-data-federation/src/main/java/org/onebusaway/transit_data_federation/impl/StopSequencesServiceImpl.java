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
package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.collections.FactoryMap;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.services.StopSequencesService;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.stereotype.Component;

@Component
public class StopSequencesServiceImpl implements StopSequencesService {

  private static final AgencyAndId NO_SHAPE_ID = new AgencyAndId("no agency",
      StopSequencesServiceImpl.class.getName() + ".noShapeId");

  private static final String NO_DIRECTION_ID = StopSequencesServiceImpl.class.getName()
      + ".noDirectionId";

  @Override
  public List<StopSequence> getStopSequencesForTrips(List<BlockTripEntry> trips) {

    Map<StopSequenceKey, List<BlockTripEntry>> tripsByStopSequenceKey = new FactoryMap<StopSequenceKey, List<BlockTripEntry>>(
        new ArrayList<BlockTripEntry>());

    for (BlockTripEntry blockTrip : trips) {
      TripEntry trip = blockTrip.getTrip();
      String directionId = trip.getDirectionId();
      if (directionId == null)
        directionId = NO_DIRECTION_ID;
      AgencyAndId shapeId = trip.getShapeId();
      if (shapeId == null || !shapeId.hasValues())
        shapeId = NO_SHAPE_ID;
      List<StopEntry> stops = getStopTimesAsStops(trip.getStopTimes());
      StopSequenceKey key = new StopSequenceKey(stops, directionId, shapeId);
      tripsByStopSequenceKey.get(key).add(blockTrip);
    }

    List<StopSequence> sequences = new ArrayList<StopSequence>();

    for (Map.Entry<StopSequenceKey, List<BlockTripEntry>> entry : tripsByStopSequenceKey.entrySet()) {
      StopSequenceKey key = entry.getKey();
      StopSequence ss = new StopSequence();
      ss.setId(sequences.size());
      ss.setRoute(null);
      ss.setStops(key.getStops());
      if (!key.getDirectionId().equals(NO_DIRECTION_ID))
        ss.setDirectionId(key.getDirectionId());
      if (!key.getShapeId().equals(NO_SHAPE_ID))
        ss.setShapeId(key.getShapeId());
      ss.setTrips(entry.getValue());
      ss.setTripCount(entry.getValue().size());
      sequences.add(ss);
    }

    return sequences;
  }

  private List<StopEntry> getStopTimesAsStops(List<StopTimeEntry> stopTimes) {
    List<StopEntry> stops = new ArrayList<StopEntry>(stopTimes.size());
    for (StopTimeEntry st : stopTimes)
      stops.add(st.getStop());
    return stops;
  }

  private static class StopSequenceKey {
    private List<StopEntry> stops;
    private String directionId;
    private AgencyAndId shapeId;

    public StopSequenceKey(List<StopEntry> stops, String directionId,
        AgencyAndId shapeId) {
      this.stops = stops;
      this.directionId = directionId;
      this.shapeId = shapeId;
    }

    public List<StopEntry> getStops() {
      return stops;
    }

    public String getDirectionId() {
      return directionId;
    }

    public AgencyAndId getShapeId() {
      return shapeId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
          + ((directionId == null) ? 0 : directionId.hashCode());
      result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
      result = prime * result + ((stops == null) ? 0 : stops.hashCode());
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
      StopSequenceKey other = (StopSequenceKey) obj;
      if (directionId == null) {
        if (other.directionId != null)
          return false;
      } else if (!directionId.equals(other.directionId))
        return false;
      if (shapeId == null) {
        if (other.shapeId != null)
          return false;
      } else if (!shapeId.equals(other.shapeId))
        return false;
      if (stops == null) {
        if (other.stops != null)
          return false;
      } else if (!stops.equals(other.stops))
        return false;
      return true;
    }
  }

}
