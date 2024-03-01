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
package org.onebusaway.transit_data_federation.impl.narrative;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.model.narrative.*;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NarrativeProviderImpl implements Serializable {

  private static final long serialVersionUID = 2L;


  private Map<String, AgencyNarrative> _agencyNarratives = new HashMap<String, AgencyNarrative>();

  private Map<AgencyAndId, StopNarrative> _stopNarratives = new HashMap<AgencyAndId, StopNarrative>();

  private Map<AgencyAndId, RouteCollectionNarrative> _routeCollectionNarratives = new HashMap<AgencyAndId, RouteCollectionNarrative>();

  private Map<AgencyAndId, TripNarrative> _tripNarratives = new HashMap<AgencyAndId, TripNarrative>();

  private Map<AgencyAndId, List<StopTimeNarrative>> _stopTimeNarrativesByTripIdAndStopTimeSequence = new HashMap<AgencyAndId, List<StopTimeNarrative>>();

  private final Map<RoutePattern, List<StopTimeNarrative>> _patternToStopTimeNarratives = new HashMap<>();

  private Map<AgencyAndId, ShapePoints> _shapePointsById = new HashMap<AgencyAndId, ShapePoints>();

  Map<AgencyAndId, ShapePoints> _dynamicShapesById = new HashMap<>();

  private Map<StopDirectionKey, RouteAndHeadsignNarrative> _patternCache = new HashMap<>();

  private Map<AgencyAndId, List<AgencyAndId>> _staticRoutesByStopId = new HashMap<>();

  @Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
  public void reset() {
//    _dynamicShapesById.clear();
  }

    public void setNarrativeForAgency(String agencyId, AgencyNarrative narrative) {
    _agencyNarratives.put(agencyId, narrative);
  }

  public void setNarrativeForStop(AgencyAndId stopId, StopNarrative narrative) {
    _stopNarratives.put(stopId, narrative);
  }

  public void setNarrativeForRouteCollectionId(AgencyAndId id,
      RouteCollectionNarrative narrative) {
    _routeCollectionNarratives.put(id, narrative);
  }

  public void setNarrativeForTripId(AgencyAndId tripId, TripNarrative narrative) {
    _tripNarratives.put(tripId, narrative);
  }

  public void setNarrativeForStopTimeEntry(AgencyAndId tripId, int index,
      StopTimeNarrative narrative) {

    List<StopTimeNarrative> narratives = _stopTimeNarrativesByTripIdAndStopTimeSequence.get(tripId);
    if (narratives == null) {
      narratives = new ArrayList<StopTimeNarrative>();
      _stopTimeNarrativesByTripIdAndStopTimeSequence.put(tripId, narratives);
    }

    while (narratives.size() <= index)
      narratives.add(null);
    narratives.set(index, narrative);

  }


  public void setShapePointsForId(AgencyAndId shapeId, ShapePoints shapePoints) {
    _shapePointsById.put(shapeId, shapePoints);
  }
  
  public AgencyNarrative getNarrativeForAgencyId(String agencyId) {
    return _agencyNarratives.get(agencyId);
  }
  
  public RouteCollectionNarrative getNarrativeForRouteCollectionId(AgencyAndId routeCollectionId) {
    return _routeCollectionNarratives.get(routeCollectionId);
  }

  public StopNarrative getNarrativeForStopId(AgencyAndId stopId) {
    return _stopNarratives.get(stopId);
  }

  public StopTimeNarrative getNarrativeForStopTimeEntry(StopTimeEntry entry) {
    // if we have an override use it first
    StopTimeNarrative narrative = getNarrativeFromPattern(entry);
    if (narrative != null) {
      return narrative;
    }
    TripEntry trip = entry.getTrip();
    int index = entry.getSequence();
    List<StopTimeNarrative> narratives = _stopTimeNarrativesByTripIdAndStopTimeSequence.get(trip.getId());

    if (narratives == null) {
      return null;
    }
    return narratives.get(index);
  }


  /**
   * return pattern name convention if present.
   * @param entry
   * @return
   */
  private StopTimeNarrative getNarrativeFromPattern(StopTimeEntry entry) {
    AgencyAndId stopId = entry.getStop().getId();
    String directionId = entry.getTrip().getDirectionId();
    // we don't use route for now
    StopDirectionKey sd = new StopDirectionKey(stopId, directionId);
    RouteAndHeadsignNarrative rd =_patternCache.get(sd);
    if (rd == null) return null;
    StopTimeNarrative.Builder builder = new StopTimeNarrative.Builder();
    builder.setStopHeadsign(rd.getHeadsign());
    builder.setRouteShortName(rd.getRouteShortname());
    return builder.create();
  }

  public RouteCollectionNarrative getRouteCollectionNarrativeForId(
      AgencyAndId routeCollectionId) {
    return _routeCollectionNarratives.get(routeCollectionId);
  }

  public TripNarrative getNarrativeForTripId(AgencyAndId tripId) {
    return _tripNarratives.get(tripId);
  }

  public ShapePoints getShapePointsForId(AgencyAndId id) {
    if (_shapePointsById.containsKey(id))
     return _shapePointsById.get(id);
    if (_dynamicShapesById == null) return null;
    return _dynamicShapesById.get(id);
  }

  public List<StopTimeNarrative> getStopTimeNarrativesForPattern(AgencyAndId routeId, String directionId, List<AgencyAndId> stopIds) {
    if (_patternToStopTimeNarratives == null) return null;
    List<StopTimeNarrative> results = _patternToStopTimeNarratives.get(new RoutePattern(routeId, directionId, stopIds));
    return results;
  }

  private List<StopTimeNarrative> find(List<AgencyAndId> stopIds, List<StopPattern> patterns) {
    for (StopPattern pattern : patterns) {
      if (pattern.stopIds == null) continue;
      if (CollectionUtils.isEqualCollection(pattern.stopIds, stopIds))
        return pattern.narratives;
      int subListLocation = Collections.indexOfSubList(pattern.stopIds, stopIds);
      if (subListLocation > -1) {
        return pattern.narratives;
      }
    }
    return null;
  }

  public void setNarrativesForStops(AgencyAndId routeId, String directionId, List<AgencyAndId> stopIds, List<StopTimeNarrative> narratives) {
    if (stopIds.size() != narratives.size())
      throw new IllegalStateException("mismatch between pattern and narratives");
    RoutePattern pattern = new RoutePattern(routeId, directionId, stopIds);
    if (!_patternToStopTimeNarratives.containsKey(pattern))
      _patternToStopTimeNarratives.put(pattern, narratives);
  }


  public void addShapePoints(ShapePoints shapePoints) {
    _dynamicShapesById.put(shapePoints.getShapeId(), shapePoints);
  }

  public void addStaticRoute(AgencyAndId stopId, List<AgencyAndId> staticRouteIds) {
    _staticRoutesByStopId.put(stopId, staticRouteIds);
  }

  public List<AgencyAndId> getStaticRoutes(AgencyAndId stopId) {
    return _staticRoutesByStopId.get(stopId);
  }

  public static class RoutePattern implements Serializable {
    private AgencyAndId routeId;
    private String directionId;
    private List<AgencyAndId> stopIds;
    public RoutePattern(AgencyAndId routeId, String direction, List<AgencyAndId> stopIds) {
      if (routeId == null) throw new NullPointerException("routeId cannot be null");
      if (direction == null) direction = "null";
      if (stopIds == null) throw new NullPointerException("stopIds cannot be null, but can be empty");
      this.routeId = routeId;
      this.directionId = direction;
      this.stopIds = stopIds;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof RoutePattern))
        return false;
      RoutePattern rd = (RoutePattern) obj;
      return rd.routeId.equals(routeId)
              && rd.directionId.equals(directionId)
              && CollectionUtils.isEqualCollection(rd.stopIds, stopIds);
    }

    @Override
    public int hashCode() {
      return routeId.hashCode() + directionId.hashCode() + stopIds.hashCode();
    }
  }

  public static class RouteDirection implements Serializable {
    private AgencyAndId routeId;
    private String directionId;

    public RouteDirection(AgencyAndId routeId, String directionId) {
      if (routeId == null) throw new NullPointerException("routeId cannot be null");
      if (directionId == null) directionId = "null";
      this.routeId = routeId;
      this.directionId = directionId;
    }
    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof RouteDirection))
        return false;
      RouteDirection rd = (RouteDirection) obj;
      return rd.routeId.equals(routeId)
              && rd.directionId.equals(directionId);
    }

    @Override
    public int hashCode() {
      return routeId.hashCode() + directionId.hashCode();
    }

    public String toString() {
      return "{" + routeId + ":"  + directionId + ")";
    }
  }

  public static class StopPattern implements Serializable {
    private List<AgencyAndId> stopIds;
    private List<StopTimeNarrative> narratives;
    public StopPattern(List<AgencyAndId> stopIds, List<StopTimeNarrative> narratives) {
      this.stopIds = stopIds;
      this.narratives = narratives;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof StopPattern))
        return false;
      StopPattern sp = (StopPattern) obj;
      return sp.stopIds.equals(stopIds)
              && sp.narratives.equals(narratives);
    }

    @Override
    public int hashCode() {
      return stopIds.hashCode() + narratives.hashCode();
    }
  }

  public void addRouteAndHeadsign(StopDirectionKey stopDirectionKey, RouteAndHeadsignNarrative routeAndHeadsignNarrative) {
    _patternCache.put(stopDirectionKey, routeAndHeadsignNarrative);
  }

  public int getPatternCount() {
    return _patternCache.size();
  }

  public StopTimeNarrative getStopTimeNarrativeForPattern(AgencyAndId routeId, AgencyAndId stopId, String directionId) {
    // we don't use route for now
    StopDirectionKey key = new StopDirectionKey(stopId, directionId);
    RouteAndHeadsignNarrative routeAndHeadsignNarrative = _patternCache.get(key);
    if (routeAndHeadsignNarrative == null) {
      for (StopDirectionKey stopDirectionKey : _patternCache.keySet()) {
        if (stopDirectionKey.getStopId().equals(stopId)) {
          System.out.println("patternCache miss but stop/direction exists=" +
                  stopDirectionKey.getStopId() + ", " + stopDirectionKey.getDirectionId());
        }
      }
      return null;
    }
    StopTimeNarrative.Builder narrative = StopTimeNarrative.builder();
    narrative.setStopHeadsign(routeAndHeadsignNarrative.getHeadsign());
    narrative.setRouteShortName(routeAndHeadsignNarrative.getRouteShortname());
    return narrative.create();
  }
}
