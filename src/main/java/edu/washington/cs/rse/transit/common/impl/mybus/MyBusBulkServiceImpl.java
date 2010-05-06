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
package edu.washington.cs.rse.transit.common.impl.mybus;

import edu.washington.cs.rse.transit.common.MetroKCDAO;
import edu.washington.cs.rse.transit.common.impl.multi.MultiContext;
import edu.washington.cs.rse.transit.common.impl.multi.MultiOperation;
import edu.washington.cs.rse.transit.common.impl.multi.MultiRunner;
import edu.washington.cs.rse.transit.common.model.Timepoint;
import edu.washington.cs.rse.transit.common.model.aggregate.BusArrivalEstimateBean;
import edu.washington.cs.rse.transit.common.services.MyBusBulkService;
import edu.washington.cs.rse.transit.common.services.MyBusService;
import edu.washington.cs.rse.transit.common.services.NoSuchStopException;
import edu.washington.cs.rse.transit.common.spring.PostConstruct;
import edu.washington.cs.rse.transit.common.spring.PreDestroy;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MyBusBulkServiceImpl implements MyBusBulkService {

  private static Logger _log = Logger.getLogger(MyBusBulkServiceImpl.class.getName());

  private static final int THREAD_COUNT = 6;

  private static final long UPDATE_AFTER = 60 * 1000;

  /***************************************************************************
   * Private Members
   **************************************************************************/

  private MetroKCDAO _dao;

  private MyBusService _mybus;

  private Cache _cache;

  private Set<Integer> _validTimepoints = new HashSet<Integer>();

  /***************************************************************************
   * 
   **************************************************************************/

  private MultiRunner<Integer> _runner;

  /***************************************************************************
   * Public Methods
   **************************************************************************/

  @Autowired
  public void setMetroKC(MetroKCDAO dao) {
    _dao = dao;
  }

  @Autowired
  public void setMyBusService(MyBusService mybus) {
    _mybus = mybus;
  }

  public void setCacheManager(CacheManager cacheManager) {
    _cache = cacheManager.getCache(MyBusBulkServiceImpl.class.getName());
    if (_cache == null) {
      _log.warning("no cache specified for "
          + MyBusBulkServiceImpl.class.getName());
      cacheManager.addCache(MyBusBulkServiceImpl.class.getName());
      _cache = cacheManager.getCache(MyBusBulkServiceImpl.class.getName());
    }
  }

  @PostConstruct
  public void startup() {

    System.out.println("======= ======== ======= go");

    List<Timepoint> timepoints = _dao.getAllTimepoints();

    for (Timepoint tp : timepoints)
      _validTimepoints.add(tp.getId());

    _log.info("Querying " + _validTimepoints.size() + " timepoint entries");

    _runner = MultiRunner.create(THREAD_COUNT, new MyBusQueryOperation(),
        _validTimepoints);
    _runner.start();
  }

  @PreDestroy
  public void shutdown() {

    _log.info("shutdown");
    _runner.doExit();
    _runner.waitForExit();
  }

  /***************************************************************************
   * {@link MyBusBulkService} Interface
   **************************************************************************/

  public int getMyBusWindowPostInMinutes() {
    return _mybus.getMyBusWindowPostInMinutes();
  }

  public int getMyBusWindowPreInMinutes() {
    return _mybus.getMyBusWindowPreInMinutes();
  }

  public long getMetroTime() throws IOException {
    return _mybus.getMetroTime();
  }

  @SuppressWarnings("unchecked")
  public List<BusArrivalEstimateBean> getSchedule(int mybusId)
      throws IOException, NoSuchStopException {

    if (!isValidTimepoint(mybusId))
      return new ArrayList<BusArrivalEstimateBean>();

    Element element = _cache.get(mybusId);

    if (element == null)
      return _mybus.getSchedule(mybusId);

    return (List<BusArrivalEstimateBean>) element.getValue();
  }

  public List<BusArrivalEstimateBean> getSchedules() {

    List<BusArrivalEstimateBean> estimates = new ArrayList<BusArrivalEstimateBean>();
    Set<Integer> timepoints = getValidTimepoints();

    for (Integer key : timepoints) {
      try {
        List<BusArrivalEstimateBean> beans = getSchedule(key);
        for (BusArrivalEstimateBean bean : beans)
          estimates.add(bean);
      } catch (Exception ex) {

      }
    }
    return estimates;
  }

  /***************************************************************************
   * Private Methods
   **************************************************************************/

  private boolean isValidTimepoint(int timepointId) {
    synchronized (_validTimepoints) {
      return _validTimepoints.contains(timepointId);
    }
  }

  private Set<Integer> getValidTimepoints() {
    synchronized (_validTimepoints) {
      return new HashSet<Integer>(_validTimepoints);
    }
  }

  private void removeValidTimepoint(int timepointId) {
    synchronized (_validTimepoints) {
      _validTimepoints.remove(timepointId);
    }
  }

  private class MyBusQueryOperation implements MultiOperation<Integer> {

    private int _count = 0;

    private long _time = System.currentTimeMillis();

    public void evaluate(MultiContext<Integer> context, Integer timepointId) {
      try {

        List<BusArrivalEstimateBean> results = _mybus.getSchedule(timepointId);
        _cache.put(new Element(timepointId, results));
        context.add(timepointId, UPDATE_AFTER);
        increment();

      } catch (NoSuchStopException ex) {
        _log.log(Level.WARNING, "no such timepoint=" + timepointId);
        removeValidTimepoint(timepointId);
      } catch (Exception ex) {
        _log.log(Level.WARNING, "error with timepoint=" + timepointId, ex);
        removeValidTimepoint(timepointId);
      }
    }

    private synchronized void increment() {
      _count++;
      long now = System.currentTimeMillis();
      if (_time + UPDATE_AFTER < now) {
        _log.info("MyBus Query Rate=" + _count);
        _count = 0;
        _time = now;
      }
    }
  }

}
