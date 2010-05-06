package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.layers.model.Layer;
import org.onebusaway.layers.model.Region;

import com.vividsolutions.jts.geom.Point;

import java.util.SortedMap;

public interface LocationNamingStrategy {
  public SortedMap<Layer, Region> getRegionsByLocation(Point location);
}
