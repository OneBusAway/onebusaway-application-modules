package org.onebusaway.metrokc2gtfs.impl;

import com.vividsolutions.jts.geom.Point;

import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.Region;

import java.util.SortedMap;

public interface LocationNamingStrategy {
  public SortedMap<Layer, Region> getRegionsByLocation(Point location);
}
