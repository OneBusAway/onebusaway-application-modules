package org.onebusaway.transit_data_federation.impl.walkplanner;

import java.util.Collection;
import java.util.Collections;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkNodeEntry;
import org.onebusaway.transit_data_federation.services.walkplanner.WalkPlannerGraph;

public class StreetGraphLibrary {

  public static Collection<WalkNodeEntry> getNodesNearLocation(
      WalkPlannerGraph graph, double lat, double lon,
      double initialSearchRadius, double maxRadius) {

    double radius = initialSearchRadius;

    while (true) {
      CoordinateBounds bounds = SphericalGeometryLibrary.bounds(lat, lon,
          radius);
      Collection<WalkNodeEntry> nodes = graph.getNodesByLocation(bounds);
      if (!nodes.isEmpty())
        return nodes;
      if (radius == maxRadius)
        return Collections.emptyList();
      radius = Math.min(radius * 2, maxRadius);
    }
  }
}
