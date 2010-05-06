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
import edu.washington.cs.rse.transit.common.model.ServicePattern;
import edu.washington.cs.rse.transit.common.model.aggregate.ServicePatternTimeBlock;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternTimeBlockBean;
import edu.washington.cs.rse.transit.web.oba.common.client.model.ServicePatternTimeBlocksBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchRouteServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.ServiceException;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Component
public class ServicePatternTimeBlocksOp extends ApplicationBeanSupport
    implements CacheOp<Integer, ServicePatternTimeBlocksBean> {

  private static final TimeZone _tz = TimeZone.getTimeZone("US/Pacific");

  private MultiRunner<Route> _runner;

  /***************************************************************************
   * {@link CacheOp} Interface
   **************************************************************************/

  public ServicePatternTimeBlocksBean evaluate(Integer routeNumber)
      throws ServiceException {

    Route route = _dao.getRouteByNumber(routeNumber);

    if (route == null)
      throw new NoSuchRouteServiceException();

    return getTimeBlocksByRoute(route);
  }

  public void startup(final Cache cache) {

    MultiOperation<Route> op = new MultiOperation<Route>() {
      public void evaluate(MultiContext<Route> context, Route route) {
        if (cache.isKeyInCache(route.getId()))
          return;
        ServicePatternTimeBlocksBean bean = getTimeBlocksByRoute(route);
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

  private ServicePatternTimeBlocksBean getTimeBlocksByRoute(Route route) {

    List<ServicePatternTimeBlock> blocks = _dao.getActiveSegmentedServicePatternTimeBlocksByRoute(route);

    Map<ServicePattern, ServicePatternBean> servicePatterns = new HashMap<ServicePattern, ServicePatternBean>();
    ServicePatternTimeBlocksBean result = new ServicePatternTimeBlocksBean();

    for (ServicePatternTimeBlock block : blocks) {
      ServicePattern sp = block.getServicePattern();
      ServicePatternBean spb = servicePatterns.get(sp);
      if (spb == null) {
        spb = getServicePatternAsBean(sp);
        servicePatterns.put(sp, spb);
      }
      ServicePatternTimeBlockBean bean = getServicePatternTimeBlockAsBean(
          block, spb);
      result.addBlock(bean);
    }

    long day = getStartOfDataAsMilliseconds();
    result.setStartOfDay(day);

    return result;
  }

  private long getStartOfDataAsMilliseconds() {

    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(_tz);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar.getTimeInMillis();
  }

}
