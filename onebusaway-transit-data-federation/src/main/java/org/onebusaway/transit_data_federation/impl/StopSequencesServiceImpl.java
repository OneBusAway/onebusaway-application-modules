package org.onebusaway.transit_data_federation.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.transit_data_federation.model.StopSequence;
import org.onebusaway.transit_data_federation.services.StopSequencesService;

import edu.washington.cs.rse.collections.FactoryMap;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class StopSequencesServiceImpl implements StopSequencesService {

  private static final AgencyAndId NO_SHAPE_ID = new AgencyAndId("no agency", StopSequencesServiceImpl.class.getName()
      + ".noShapeId");

  private static final String NO_DIRECTION_ID = StopSequencesServiceImpl.class.getName() + ".noDirectionId";

  /* (non-Javadoc)
   * @see org.onebusaway.transit_data_federation.impl.StopSequencesService#getStopSequencesForTrips(java.util.Map)
   */
  public List<StopSequence> getStopSequencesForTrips(Map<Trip, List<StopTime>> stopTimesByTrip) {

    Map<StopSequenceKey, List<Trip>> tripsByStopSequenceKey = new FactoryMap<StopSequenceKey, List<Trip>>(
        new ArrayList<Trip>());

    for (Map.Entry<Trip, List<StopTime>> entry : stopTimesByTrip.entrySet()) {
      Trip trip = entry.getKey();
      List<StopTime> stopTimes = entry.getValue();
      String directionId = trip.getDirectionId();
      if (directionId == null)
        directionId = NO_DIRECTION_ID;
      AgencyAndId shapeId = trip.getShapeId();
      if (shapeId == null || !shapeId.hasValues())
        shapeId = NO_SHAPE_ID;
      Collections.sort(stopTimes);
      List<Stop> stops = getStopTimesAsStops(stopTimes);
      StopSequenceKey key = new StopSequenceKey(stops, directionId, shapeId);
      tripsByStopSequenceKey.get(key).add(trip);
    }

    List<StopSequence> sequences = new ArrayList<StopSequence>();

    for (Map.Entry<StopSequenceKey, List<Trip>> entry : tripsByStopSequenceKey.entrySet()) {
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

  private List<Stop> getStopTimesAsStops(List<StopTime> stopTimes) {
    List<Stop> stops = new ArrayList<Stop>(stopTimes.size());
    for (StopTime st : stopTimes)
      stops.add(st.getStop());
    return stops;
  }

  private static class StopSequenceKey {
    private List<Stop> stops;
    private String directionId;
    private AgencyAndId shapeId;

    public StopSequenceKey(List<Stop> stops, String directionId, AgencyAndId shapeId) {
      this.stops = stops;
      this.directionId = directionId;
      this.shapeId = shapeId;
    }

    public List<Stop> getStops() {
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
      result = prime * result + ((directionId == null) ? 0 : directionId.hashCode());
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
