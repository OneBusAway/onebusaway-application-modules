package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.layers.model.Layer;
import org.onebusaway.layers.model.LayersAndRegions;
import org.onebusaway.layers.model.Region;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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
