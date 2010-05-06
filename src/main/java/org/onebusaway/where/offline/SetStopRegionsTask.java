/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.offline;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;

import org.onebusaway.common.model.Layer;
import org.onebusaway.common.model.Region;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.where.model.StopRegion;
import org.onebusaway.where.services.WhereDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SetStopRegionsTask implements Runnable {

  private static int UPDATE_BUFFER_SIZE = 50;

  private GtfsDao _gtfsDao;

  private WhereDao _whereDao;

  @Autowired
  public void setGtfsDao(GtfsDao dao) {
    _gtfsDao = dao;
  }

  @Autowired
  public void setWhereDao(WhereDao dao) {
    _whereDao = dao;
  }

  public void run() {

    List<Stop> stops = getStops();

    List<StopRegion> updates = new ArrayList<StopRegion>(UPDATE_BUFFER_SIZE);

    int index = 0;

    for (Stop stop : stops) {

      if (index % 100 == 0)
        System.out.println("stops=" + index + "/" + stops.size());
      index++;

      Point p = stop.getLocation();
      List<Region> regions = _whereDao.getRegionsByLocation(p);
      System.out.println("regions=" + regions.size());

      Map<Layer, Region> byLayer = new HashMap<Layer, Region>();

      for (Region region : regions) {
        boolean contains = false;
        Geometry boundary = region.getBoundary();

        try {
          contains = boundary.contains(stop.getLocation());
        } catch (TopologyException ex) {
          throw new IllegalStateException("error with region="
              + region.getName(), ex);
        }

        if (!contains)
          continue;

        Layer layer = region.getLayer();
        Region r = byLayer.get(layer);
        if (r == null
            || getDistance(p, region.getBoundary()) < getDistance(p,
                r.getBoundary()))
          byLayer.put(layer, region);
      }

      for (Region r : byLayer.values())
        updates.add(new StopRegion(stop, r));
      if (updates.size() >= UPDATE_BUFFER_SIZE)
        flush(updates);
    }

    if (!updates.isEmpty())
      flush(updates);
  }

  private List<Stop> getStops() {
    return _gtfsDao.getAllStops();
  }

  private double getDistance(Geometry from, Geometry to) {
    return from.getCentroid().distance(to.getCentroid());
  }

  private void flush(List<StopRegion> updates) {
    System.out.println("flush...");
    _whereDao.saveOrUpdateAllEntities(updates);
    updates.clear();
  }
}
