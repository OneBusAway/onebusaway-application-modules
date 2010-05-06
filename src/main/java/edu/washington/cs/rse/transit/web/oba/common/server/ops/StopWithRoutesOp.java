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

import edu.washington.cs.rse.transit.common.impl.multi.MultiContext;
import edu.washington.cs.rse.transit.common.impl.multi.MultiOperation;
import edu.washington.cs.rse.transit.common.impl.multi.MultiRunner;
import edu.washington.cs.rse.transit.common.model.Route;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.web.oba.common.client.model.RouteBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopWithRoutesBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchStopServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.ServiceException;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StopWithRoutesOp extends ApplicationBeanSupport implements
    CacheOp<Integer, StopWithRoutesBean> {

  private MultiRunner<StopLocation> _runner;

  public StopWithRoutesBean evaluate(Integer stopId) throws ServiceException {

    StopLocation stop = _dao.getStopLocationById(stopId);
    if (stop == null)
      throw new NoSuchStopServiceException();
    return getBean(stop);
  }

  public void startup(final Cache cache) {

    MultiOperation<StopLocation> op = new MultiOperation<StopLocation>() {
      public void evaluate(MultiContext<StopLocation> context, StopLocation stop) {

        if (cache.isKeyInCache(stop.getId()))
          return;

        StopWithRoutesBean bean = getBean(stop);
        cache.put(new Element(stop.getId(), bean));
      }
    };

    // Pre-Cache
    _dao.getAllRoutes();

    _runner = MultiRunner.create(1, op, _dao.getAllStopLocations());
    _runner.start();
  }

  public void shutdown() {
    _runner.waitForExit();
  }

  private StopWithRoutesBean getBean(StopLocation stop) {

    List<Route> routes = _dao.getActiveRoutesByStopId(stop.getId());

    StopBean stopBean = getStopAsBean(stop);

    List<RouteBean> routeBeans = new ArrayList<RouteBean>(routes.size());
    for (Route route : routes)
      routeBeans.add(getRouteAsBean(route));

    StopWithRoutesBean result = new StopWithRoutesBean();

    result.setStop(stopBean);
    result.setRoutes(routeBeans);

    return result;
  }

}
