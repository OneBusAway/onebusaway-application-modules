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
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.transit.common.impl.multi.MultiContext;
import edu.washington.cs.rse.transit.common.impl.multi.MultiOperation;
import edu.washington.cs.rse.transit.common.impl.multi.MultiRunner;
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.TransLinkShapePoint;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternPathBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchServicePatternServiceException;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import com.vividsolutions.jts.geom.Point;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServicePatternPathOp extends ApplicationBeanSupport implements
    CacheOp<Integer, ServicePatternPathBean> {

  private static final TransLinkShapePointToPointAdapter POINT_ADAPTER = new TransLinkShapePointToPointAdapter();

  private MultiRunner<ServicePattern> _runner;

  public ServicePatternPathBean evaluate(Integer servicePatternId)
      throws NoSuchServicePatternServiceException {

    ServicePattern pattern = _dao.getActiveServicePatternById(servicePatternId);

    if (pattern == null)
      throw new NoSuchServicePatternServiceException();

    return getServicePatternPathBean(pattern);
  }

  public void startup(final Cache cache) {
    MultiOperation<ServicePattern> op = new MultiOperation<ServicePattern>() {

      private int _count = 0;

      public void evaluate(MultiContext<ServicePattern> context,
          ServicePattern pattern) {

        int id = pattern.getId().getId();

        if (cache.get(id) != null)
          return;

        ServicePatternPathBean bean = getServicePatternPathBean(pattern);

        Element element = new Element(id, bean);
        cache.put(element);

        synchronized (this) {
          _count++;
          System.out.println("service pattern path op=" + _count + " key="
              + pattern.getId());
        }
      }
    };

    // Pre-cache
    _dao.getAllRoutes();

    _runner = MultiRunner.create(1, op, _dao.getActiveServicePatterns());
    _runner.start();
  }

  public void shutdown() {
    _runner.waitForExit();
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private ServicePatternPathBean getServicePatternPathBean(
      ServicePattern pattern) {

    List<TransLinkShapePoint> links = _dao.getTransLinkShapePointsByServicePattern(pattern);

    Iterable<Point> points = AdapterLibrary.adapt(links, POINT_ADAPTER);

    List<CoordinatePoint> cPoints = _dao.getPointsAsLatLongs(points,
        links.size());

    double[] lat = new double[cPoints.size()];
    double[] lon = new double[cPoints.size()];
    int index = 0;
    for (CoordinatePoint p : cPoints) {
      lat[index] = p.getLat();
      lon[index] = p.getLon();
      index++;
    }

    ServicePatternPathBean path = new ServicePatternPathBean(
        getServicePatternAsBean(pattern), lat, lon);
    return path;
  }

}
