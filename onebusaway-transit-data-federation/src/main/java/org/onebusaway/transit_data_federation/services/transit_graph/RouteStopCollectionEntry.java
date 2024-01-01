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
package org.onebusaway.transit_data_federation.services.transit_graph;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.RouteStop;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Canonical equivalent of RouteCollectionEntry -- a grouping
 * of ideal stops based on a routeId.
 */
public class RouteStopCollectionEntry implements Serializable {

  private static Logger _log = LoggerFactory.getLogger(RouteStopCollectionEntry.class);
  private AgencyAndId routeId;
  private String id;
  private String name;

  // keep in sorted order
  private Collection<StopAndSequence> routeStops = new TreeSet<StopAndSequence>(new StopAndSequenceComparator());

  public void add(RouteStop routeStop) {
    try {
      char separator = AgencyAndId.ID_SEPARATOR;
      if (routeStop.getStopId().indexOf(separator) == -1)
        separator = CanonicalRoutesEntry.ALT_ID_SEPARATOR;
      AgencyAndId stopId = AgencyAndIdLibrary.convertFromString(routeStop.getStopId(), separator);
      routeStops.add(new StopAndSequence(stopId, routeStop.getStopSequence()));
    } catch (IllegalStateException ise) {
      _log.error("invalid stop " + routeStop.getStopId() + " not in AgencyAndId format");
    }
  }

  public AgencyAndId getRouteId() {
    return routeId;
  }

  public void setRouteId(AgencyAndId routeId) {
    this.routeId = routeId;
  }

  public Collection<StopAndSequence> getRouteStops() {
    return routeStops;
  }

  public void setRouteStops(Collection<StopAndSequence> routeStops) {
    this.routeStops.addAll(routeStops);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static class StopAndSequence implements Serializable {
    private AgencyAndId stopId;
    private int sequence;
    public StopAndSequence(AgencyAndId stopId, int sequence) {
      this.stopId = stopId;
      this.sequence = sequence;
    }

    public AgencyAndId getStopId() {
      return stopId;
    }
    public int getSequence() {
      return sequence;
    }
  }

  public static class StopAndSequenceComparator implements Comparator<StopAndSequence>, Serializable {

    @Override
    public int compare(StopAndSequence o1, StopAndSequence o2) {
      return o1.sequence - o2.sequence;
    }
  }
}
