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
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.StopLocation;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.StopsBean;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StopsByServicePatternOp extends ApplicationBeanSupport implements
    CacheOp<Integer, StopsBean> {

  private MultiRunner<ServicePattern> _runner;

  /*****************************************************************************
   * {@link CacheOp} Interface
   ****************************************************************************/

  public StopsBean evaluate(Integer servicePatternId) {
    ServicePattern servicePattern = _dao.getActiveServicePatternById(servicePatternId);
    return getActiveStopsByServicePattern(servicePattern);
  }

  public void startup(final Cache cache) {

    MultiOperation<ServicePattern> op = new MultiOperation<ServicePattern>() {

      public void evaluate(MultiContext<ServicePattern> context,
          ServicePattern servicePattern) {

        int id = servicePattern.getId().getId();

        if (cache.isKeyInCache(id))
          return;

        StopsBean bean = getActiveStopsByServicePattern(servicePattern);
        cache.put(new Element(id, bean));
      }
    };

    // Pre-Cache
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

  private StopsBean getActiveStopsByServicePattern(ServicePattern servicePattern) {

    StopsBean bean = new StopsBean();
    List<StopLocation> stops = _dao.getStopLocationsByServicePattern(servicePattern);

    List<StopBean> beans = new ArrayList<StopBean>(stops.size());
    for (StopLocation stop : stops)
      beans.add(getStopAsBean(stop));

    bean.setStopBeans(beans);

    return bean;
  }

}
