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

import org.onebusaway.gtfs_realtime.archiver.model.AlertModel;
import org.onebusaway.gtfs_realtime.archiver.model.TripUpdateModel;
import org.onebusaway.gtfs_realtime.archiver.model.VehiclePositionModel;

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
  
  private ArrayBlockingQueue<TripUpdateModel> _tripUpdates = new ArrayBlockingQueue<TripUpdateModel>(100000);
  private ArrayBlockingQueue<VehiclePositionModel> _vehiclePositions = new ArrayBlockingQueue<VehiclePositionModel>(100000);
  private ArrayBlockingQueue<AlertModel> _alerts = new ArrayBlockingQueue<AlertModel>(100000);
  
  
  private ThreadPoolTaskScheduler _taskScheduler;

  private TripUpdateDao _tripUpdateDao;
  private VehiclePositionDao _vehiclePositionDao;
  private AlertDao _alertDao;
  
  @Autowired
  public void setTaskScheduler(ThreadPoolTaskScheduler scheduler) {
    _taskScheduler = scheduler;
  }
  
  @Autowired
  public void setTripUpdateDao(TripUpdateDao dao) {
    _tripUpdateDao = dao;
  }

  @Autowired
  public void setVehiclePositionDao(VehiclePositionDao dao) {
    _vehiclePositionDao = dao;
  }

  @Autowired
  public void setAlertDao(AlertDao dao) {
    _alertDao = dao;
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
    final TripUpdateThread tripUpdateThread = new TripUpdateThread();
    _taskScheduler.scheduleWithFixedDelay(tripUpdateThread, 10 * 1000); // every 10 seconds
    final VehiclePositionThread vehiclePositionThread = new VehiclePositionThread();
    _taskScheduler.scheduleWithFixedDelay(vehiclePositionThread, 10 * 1000); // every 10 seconds;
    final AlertThread alertThread = new AlertThread();
    _taskScheduler.scheduleWithFixedDelay(alertThread, 10 * 1000); // every 10 seconds;
    
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
  public void persist(TripUpdateModel tripUpdate) {
    boolean accepted =_tripUpdates.offer(tripUpdate);
    if (!accepted) {
    _log.error("Local trip update buffer full!  Clearing!  Dropping " + tripUpdate.getId() + " record");
    }
  }

  @Override
  public void persist(VehiclePositionModel vehiclePosition) {
    boolean accepted = _vehiclePositions.offer(vehiclePosition);
    if (!accepted) {
    _log.error("Local vehicle position buffer full!  Clearing!  Dropping " + vehiclePosition.getId() + " record");
    }
  }

  @Override
  public void persist(AlertModel alert) {
    boolean accepted = _alerts.offer(alert);
    if (!accepted) {
    _log.error("Local alert buffer full!  Clearing!  Dropping " + alert.getId() + " record");
    }
  }

  private class TripUpdateThread implements Runnable {
    
    @Override
    public void run() {
      List<TripUpdateModel> records = new ArrayList<TripUpdateModel>();
      _tripUpdates.drainTo(records, _batchSize);
      _log.info("drained " + records.size() + " trip updates");
      try {
        _tripUpdateDao.saveOrUpdate(records.toArray(new TripUpdateModel[0]));
      } catch (Exception e) {
        _log.error("error persisting trip updates=", e);
      }
    }
  }

  private class VehiclePositionThread implements Runnable {
    
    @Override
    public void run() {
      List<VehiclePositionModel> records = new ArrayList<VehiclePositionModel>();
      _vehiclePositions.drainTo(records, _batchSize);
      _log.info("drained " + records.size() + " vehicle positions");
      try {
        _vehiclePositionDao.saveOrUpdate(records.toArray(new VehiclePositionModel[0]));
      } catch (Exception e) {
        _log.error("error persisting vehiclePositions=", e);
      }
    }
  }
  
  private class AlertThread implements Runnable {
    
    @Override
    public void run() {
      List<AlertModel> records = new ArrayList<AlertModel>();
      _alerts.drainTo(records, _batchSize);
      _log.info("drained " + records.size() + " alerts");
      try {
        _alertDao.saveOrUpdate(records.toArray(new AlertModel[0]));
      } catch (Exception e) {
        _log.error("error persisting alerts=", e);
      }
    }
  }
  
}
