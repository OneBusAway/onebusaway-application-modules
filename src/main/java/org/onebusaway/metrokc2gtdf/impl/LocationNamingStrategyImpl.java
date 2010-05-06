package org.onebusaway.metrokc2gtdf.impl;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.LayersAndRegions;
import org.onebusaway.common.model.Region;
import org.onebusaway.metrokc2gtdf.LocationNamingStrategy;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class LocationNamingStrategyImpl implements LocationNamingStrategy {

  private LayersAndRegions _layersAndRegions;

  public void setLayersAndRegions(LayersAndRegions layersAndRegions) {
    _layersAndRegions = layersAndRegions;
  }

  public SortedMap<Layer, Region> getRegionsByLocation(Point location) {

    SortedMap<Layer, Region> m = new TreeMap<Layer, Region>();

    for (List<Region> region : _layersAndRegions.getRegions()) {

      for (Region r : region) {

        Geometry boundary = r.getBoundary();
        boundary = boundary.buffer(0);

        if (boundary.contains(location)) {
          Region rCurrent = m.get(r.getLayer());
          if (rCurrent == null
              || location.distance(boundary) < location.distance(rCurrent.getBoundary()))
            m.put(r.getLayer(), r);
        }
      }
    }

    return m;
  }
}
