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
package edu.washington.cs.rse.transit.web.oba.common.server.ops;

import edu.washington.cs.rse.collections.adapter.AdapterLibrary;
import edu.washington.cs.rse.collections.stats.Max;
import edu.washington.cs.rse.collections.tuple.Pair;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.transit.common.impl.multi.MultiContext;
import edu.washington.cs.rse.transit.common.impl.multi.MultiOperation;
import edu.washington.cs.rse.transit.common.impl.multi.MultiRunner;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.TransLinkShapePoint;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternBlock;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternBlockKey;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlockPathsBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchServicePatternServiceException;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import com.vividsolutions.jts.geom.Point;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ServicePatternPathOp extends ApplicationBeanSupport implements
    CacheOp<ServicePatternBlockKey, ServicePatternBlockPathsBean> {

  private static final TransLinkShapePointToPointAdapter POINT_ADAPTER = new TransLinkShapePointToPointAdapter();

  private MultiRunner<ServicePatternBlock> _runner;

  public ServicePatternBlockPathsBean evaluate(ServicePatternBlockKey key)
      throws NoSuchServicePatternServiceException {

    ServicePatternBlock block = _dao.getEntity(ServicePatternBlock.class, key);

    if (block == null)
      throw new NoSuchServicePatternServiceException();

    ServicePatternBlockPathsBean paths = getServicePatternBlockAsPaths(block);

    return paths;
  }

  public void startup(final Cache cache) {

    MultiOperation<ServicePatternBlock> op = new MultiOperation<ServicePatternBlock>() {

      private int _count = 0;

      public void evaluate(MultiContext<ServicePatternBlock> context,
          ServicePatternBlock block) {

        if (cache.get(block.getId()) != null)
          return;

        ServicePatternBlockPathsBean bean = getServicePatternBlockAsPaths(block);

        Element element = new Element(block.getId(), bean);
        cache.put(element);

        synchronized (this) {
          _count++;
          System.out.println("service pattern path op=" + _count + " key="
              + block.getId());
        }
      }
    };

    // Pre-cache
    _dao.getAllRoutes();

    _runner = MultiRunner.create(1, op, _dao.getAllServicePatternBlocks());
    _runner.start();
  }

  public void shutdown() {
    _runner.waitForExit();
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private ServicePatternBlockPathsBean getServicePatternBlockAsPaths(ServicePatternBlock block) {

    Set<Integer> prevVisited = new HashSet<Integer>();
    List<TransLinkShapePoint> path = new ArrayList<TransLinkShapePoint>();

    ServicePatternBlockPathsBean paths = new ServicePatternBlockPathsBean();

    Max<Pair<TransLinkShapePoint>> m = new Max<Pair<TransLinkShapePoint>>();
    
    for (ServicePattern pattern : block.getServicePatterns()) {

      Set<Integer> visited = new HashSet<Integer>();
      List<TransLinkShapePoint> links = _dao.getTransLinkShapePointsByServicePattern(pattern);

      for (TransLinkShapePoint point : links) {

        int id = point.getId().getTransLink().getId();
        visited.add(id);

        if (prevVisited.contains(id)) {
          flushPath(paths, path);
        } else {
          path.add(point);
        }
      }
      
      TransLinkShapePoint from = links.get(0);
      TransLinkShapePoint to = links.get(links.size()-1);
      double d = from.getLocation().distance(to.getLocation());
      m.add(d, Pair.createPair(from, to));
      
      flushPath(paths, path);

      prevVisited.addAll(visited);
    }
    
    CoordinatePoint fromPoint = _dao.getPointAsLatLong(block.getStartLocation());
    CoordinatePoint toPoint = _dao.getPointAsLatLong(block.getEndLocation());
    
    paths.setStartLat(fromPoint.getLat());
    paths.setStartLon(fromPoint.getLon());
    
    paths.setEndLat(toPoint.getLat());
    paths.setEndLon(toPoint.getLon());


    return paths;
  }

  private void flushPath(ServicePatternBlockPathsBean paths, List<TransLinkShapePoint> path) {
    if (path.size() > 0) {
      PathBean pathBean = getServicePatternPathBean(path);
      paths.addPath(pathBean);
      path.clear();
    }
  }

  private PathBean getServicePatternPathBean(
      List<TransLinkShapePoint> shapePoints) {

    Iterable<Point> points = AdapterLibrary.adapt(shapePoints, POINT_ADAPTER);

    List<CoordinatePoint> cPoints = _dao.getPointsAsLatLongs(points,
        shapePoints.size());

    double[] lat = new double[cPoints.size()];
    double[] lon = new double[cPoints.size()];

    int index = 0;

    for (CoordinatePoint p : cPoints) {
      lat[index] = p.getLat();
      lon[index] = p.getLon();
      index++;
    }

    return new PathBean(lat, lon);
  }

}
