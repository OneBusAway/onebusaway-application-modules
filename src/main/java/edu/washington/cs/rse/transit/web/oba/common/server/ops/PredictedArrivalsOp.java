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
import edu.washington.cs.rse.transit.common.model.ServicePatternKey;
import edu.washington.cs.rse.transit.common.model.aggregate.InterpolatedStopTime;
import edu.washington.cs.rse.transit.common.model.aggregate.ScheduledArrivalTime;
import edu.washington.cs.rse.transit.common.model.aggregate.StopTimepointInterpolation;
import edu.washington.cs.rse.transit.common.services.CacheOp;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;
import edu.washington.cs.rse.transit.common.services.StopSchedulingService;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PredictedArrivalBean;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.NoSuchStopServiceException;
import edu.washington.cs.rse.transit.web.oba.common.client.rpc.ServiceException;
import edu.washington.cs.rse.transit.web.oba.common.server.ApplicationBeanSupport;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Component
public class PredictedArrivalsOp extends ApplicationBeanSupport implements
    CacheOp<Integer, List<PredictedArrivalBean>> {

  private Logger _log = Logger.getLogger(PredictedArrivalsOp.class.getName());

  private static final SortByStopLocation SORT_BY_STOP = new SortByStopLocation();

  private static final int SERVICE_PATTERN_BLOCK_SIZE = 100;

  @Autowired
  private StopSchedulingService _schedulingService;

  private MultiRunner<Set<ServicePatternKey>> _runner;

  public List<PredictedArrivalBean> evaluate(Integer stopId)
      throws ServiceException {
    try {
      List<ScheduledArrivalTime> sats = _schedulingService.getPredictedArrivalsByStopId(stopId);
      List<PredictedArrivalBean> beans = new ArrayList<PredictedArrivalBean>();
      for (ScheduledArrivalTime sat : sats)
        beans.add(getPredictedArrivalTimeAsBean(sat));
      return beans;
    } catch (NoSuchStopException ex) {
      throw new NoSuchStopServiceException();
    }
  }

  public void startup(Cache cache) {

    List<ServicePattern> patterns = _dao.getActiveServicePatterns();

    List<Set<ServicePatternKey>> blocks = new ArrayList<Set<ServicePatternKey>>();
    Set<ServicePatternKey> block = new HashSet<ServicePatternKey>();
    blocks.add(block);

    for (ServicePattern pattern : patterns) {
      block.add(pattern.getId());
      if (block.size() == SERVICE_PATTERN_BLOCK_SIZE) {
        block = new HashSet<ServicePatternKey>();
        blocks.add(block);
      }
    }

    _log.info("ServicePatterns=" + patterns.size() + " Blocks=" + blocks.size());

    _runner = MultiRunner.create(1, new WorkerOp(cache, blocks.size()), blocks);
    _runner.start();
  }

  public void shutdown() {
    _runner.waitForExit();
  }

  private class WorkerOp implements MultiOperation<Set<ServicePatternKey>> {

    private Cache _cache;

    private int _count = 0;

    private long _lastUpdate = System.currentTimeMillis();

    private int _blocks;

    public WorkerOp(Cache cache, int blocks) {
      _cache = cache;
      _blocks = blocks;
    }

    @SuppressWarnings("unchecked")
    public void evaluate(MultiContext<Set<ServicePatternKey>> context,
        Set<ServicePatternKey> ids) {

      List<ScheduledArrivalTime> sats = _schedulingService.getArrivalsByServicePatterns(ids);

      // We sort by stop to increase the likelihood of cache-hits
      Collections.sort(sats, SORT_BY_STOP);

      for (ScheduledArrivalTime sat : sats) {

        if (context.wantsExit())
          return;

        InterpolatedStopTime ist = (InterpolatedStopTime) sat.getStopTime();
        StopTimepointInterpolation sti = ist.getInterpolation();
        int stopId = sti.getStop().getId();
        PredictedArrivalBean pab = getPredictedArrivalTimeAsBean(sat);

        List<PredictedArrivalBean> beans = new ArrayList<PredictedArrivalBean>();

        Integer key = stopId;
        Element element = _cache.get(key);
        if (element != null)
          beans = (List<PredictedArrivalBean>) element.getValue();

        List<PredictedArrivalBean> updated = new ArrayList<PredictedArrivalBean>(
            beans.size() + 1);
        long now = System.currentTimeMillis();
        for (PredictedArrivalBean bean : beans) {
          if (bean.getMaxTime() > now - 5 * 60 * 1000
              && bean.getTripId() != pab.getTripId())
            updated.add(bean);
        }
        updated.add(pab);
        element = new Element(key, updated);
        _cache.put(element);
      }

      context.add(ids, 60 * 1000);
      update();
    }

    private synchronized void update() {
      _count++;
      long now = System.currentTimeMillis();
      if (_lastUpdate + 60 * 1000 < now) {
        double rate = ((double) _count) / (now - _lastUpdate) * (60 * 1000);
        _log.info("Processing rate=" + rate + "/min blocks=" + _blocks);
        _count = 0;
        _lastUpdate = now;
      }
    }
  }

  private static class SortByStopLocation implements
      Comparator<ScheduledArrivalTime> {

    public int compare(ScheduledArrivalTime o1, ScheduledArrivalTime o2) {
      InterpolatedStopTime ist1 = (InterpolatedStopTime) o1.getStopTime();
      InterpolatedStopTime ist2 = (InterpolatedStopTime) o2.getStopTime();

      int id1 = ist1.getInterpolation().getStop().getId();
      int id2 = ist2.getInterpolation().getStop().getId();

      return id1 == id2 ? 0 : (id1 < id2 ? -1 : 1);
    }
  }
}
