/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs_realtime.archiver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.gtfs_realtime.archiver.model.TripUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
/**
 * Handles asynchronous persistence of the archiver models. 
 */
public class GtfsPersistorImpl implements GtfsPersistor {
  private static Logger _log = LoggerFactory.getLogger(GtfsPersistorImpl.class);
  
  private ArrayBlockingQueue<TripUpdate> _messages = new ArrayBlockingQueue<TripUpdate>(100000);
  
  
  private ThreadPoolTaskScheduler _taskScheduler;

  
  private TripUpdateDao _dao;
  
  @Autowired
  public void setTaskScheduler(ThreadPoolTaskScheduler scheduler) {
    _taskScheduler = scheduler;
  }
  
  @Autowired
  public void setTripUpdateDao(TripUpdateDao dao) {
    _dao = dao;
  }
  
  /**
   * number of inserts to batch together
   */
  private int _batchSize;

  public void setBatchSize(String batchSizeStr) {
    _batchSize = Integer.decode(batchSizeStr);
  }

  @PostConstruct
  public void start() {
    _log.info("starting!");
    final SaveThread saveThread = new SaveThread();
    _taskScheduler.scheduleWithFixedDelay(saveThread, 10 * 1000); // every 10 seconds
  }
  
  @PreDestroy
  public void stop() {
    _log.info("stopping");
    if (_taskScheduler != null) {
      _taskScheduler.shutdown();
      _taskScheduler = null;
    }
    
  }
  @Override
  public void persist(TripUpdate record) {
    boolean accepted =_messages.offer(record);
    if (!accepted) {
    _log.error("Local buffer full!  Clearing!  Dropping " + record.getId() + " record");
    }
  }

  private class SaveThread implements Runnable {
    
    @Override
    public void run() {
      List<TripUpdate> records = new ArrayList<TripUpdate>();
      _messages.drainTo(records, _batchSize);
      _log.info("drained " + records.size() + " trip updates");
      try {
        _dao.saveOrUpdate(records.toArray(new TripUpdate[0]));
      } catch (Exception e) {
        _log.error("error persisting trip updates=", e);
      }
    }
  }
  
}
