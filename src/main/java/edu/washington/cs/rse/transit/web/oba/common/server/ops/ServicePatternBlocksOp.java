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
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternBlock;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlockBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBlocksBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchRouteServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.ServiceException;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServicePatternBlocksOp extends ApplicationBeanSupport implements
    CacheOp<Integer, ServicePatternBlocksBean> {

  private MultiRunner<Route> _runner;

  /***************************************************************************
   * {@link CacheOp} Interface
   **************************************************************************/

  public ServicePatternBlocksBean evaluate(Integer routeNumber)
      throws ServiceException {

    Route route = _dao.getRouteByNumber(routeNumber);

    if (route == null)
      throw new NoSuchRouteServiceException();

    return getBlocksByRoute(route);
  }

  public void startup(final Cache cache) {

    MultiOperation<Route> op = new MultiOperation<Route>() {
      public void evaluate(MultiContext<Route> context, Route route) {
        if (cache.isKeyInCache(route.getId()))
          return;
        ServicePatternBlocksBean bean = getBlocksByRoute(route);
        cache.put(new Element(route.getNumber(), bean));
      }
    };

    // Pre-Cache
    _dao.getAllServicePatterns();

    _runner = MultiRunner.create(1, op, _dao.getAllRoutes());
    _runner.start();
  }

  public void shutdown() {
    _runner.waitForExit();
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private ServicePatternBlocksBean getBlocksByRoute(Route route) {

    List<ServicePatternBlock> blocks = _dao.getServicePatternBlocksByRoute(route);

    ServicePatternBlocksBean result = new ServicePatternBlocksBean();

    for (ServicePatternBlock block : blocks) {

      ServicePatternBlockBean bean = getServicePatternBlockAsBean(block);
      result.addBlock(bean);
    }

    return result;
  }
}
