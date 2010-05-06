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
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopAreaBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchStopServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.ServiceException;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.stereotype.Component;

@Component
public class StopAreaOp extends ApplicationBeanSupport implements
    CacheOp<Integer, StopAreaBean> {

  private MultiRunner<StopLocation> _runner;

  public StopAreaBean evaluate(Integer stopId) throws ServiceException {

    StopLocation stop = _dao.getStopLocationById(stopId);
    if (stop == null)
      throw new NoSuchStopServiceException();
    return getStopAreaAsBean(stop);
  }

  public void startup(final Cache cache) {

    MultiOperation<StopLocation> op = new MultiOperation<StopLocation>() {
      public void evaluate(MultiContext<StopLocation> context, StopLocation stop) {
        if (cache.isKeyInCache(stop.getId()))
          return;
        StopAreaBean bean = getStopAreaAsBean(stop);
        cache.put(new Element(stop.getId(), bean));
      }
    };

    _runner = MultiRunner.create(1, op, _dao.getAllStopLocations());
    _runner.start();
  }

  public void shutdown() {
    _runner.waitForExit();
  }
}
