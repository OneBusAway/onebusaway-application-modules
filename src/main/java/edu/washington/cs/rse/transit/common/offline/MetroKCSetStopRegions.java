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
package edu.washington.cs.rse.transit.common.offline;

import edu.washington.cs.rse.transit.MetroKCApplicationContext;
import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.model.aggregate.Layer;
import edu.washington.cs.rse.transit.common.model.aggregate.Region;
import edu.washington.cs.rse.transit.common.model.aggregate.StopRegion;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetroKCSetStopRegions {

  public static void main(String[] args) throws Exception {
    ApplicationContext ctx = MetroKCApplicationContext.getApplicationContext();
    MetroKCSetStopRegions m = new MetroKCSetStopRegions();
    ctx.getAutowireCapableBeanFactory().autowireBean(m);
    m.run();
  }

  private MetroKCDAO _dao;

  @Autowired
  public void setMetroKCDAO(MetroKCDAO dao) {
    _dao = dao;
  }

  public void run() throws Exception {

    List<StopLocation> stops = getStops();

    int count = 1000;
    List<StopRegion> updates = new ArrayList<StopRegion>(count);

    for (StopLocation stop : stops) {
      System.out.println("stop=" + stop.getId());

      Point p = stop.getLocation();
      List<Region> regions = _dao.getRegionsByLocation(p);
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
      if (updates.size() >= count)
        flush(updates);
    }

    if (!updates.isEmpty())
      flush(updates);
  }

  private List<StopLocation> getStops() {
    return _dao.getAllStopLocations();
  }

  private double getDistance(Geometry from, Geometry to) {
    return from.getCentroid().distance(to.getCentroid());
  }

  private void flush(List<StopRegion> updates) {
    System.out.println("flush...");
    _dao.saveOrUpdateAllEntities(updates);
    updates.clear();
  }
}
