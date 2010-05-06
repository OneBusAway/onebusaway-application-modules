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
import edu.washington.cs.rse.transit.common.model.aggregate.StopSelectionTree;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.common.services.NoSuchRouteException;
import edu.washington.cs.rse.transit.common.services.StopSelectionService;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchRouteServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.ServiceException;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopSelectionTreeOp extends ApplicationBeanSupport implements
    CacheOp<Integer, StopSelectionTree> {

  @Autowired
  private StopSelectionService _stopSelection;

  private MultiRunner<Route> _runner;

  public StopSelectionTree evaluate(Integer routeId) throws ServiceException {
    try {
      return _stopSelection.getStopsByRoute(routeId);
    } catch (NoSuchRouteException e) {
      throw new NoSuchRouteServiceException();
    }
  }

  public void startup(final Cache cache) {

    MultiOperation<Route> op = new MultiOperation<Route>() {
      public void evaluate(MultiContext<Route> context, Route route) {
        try {

          if (cache.isKeyInCache(route.getId()))
            return;

          StopSelectionTree tree = _stopSelection.getStopsByRoute(route.getId());
          cache.put(new Element(route.getId(), tree));
        } catch (NoSuchRouteException ex) {

        }
      }
    };
    _runner = MultiRunner.create(1, op, _dao.getAllRoutes());
    _runner.start();
  }

  public void shutdown() {
    _runner.waitForExit();
  }

}
