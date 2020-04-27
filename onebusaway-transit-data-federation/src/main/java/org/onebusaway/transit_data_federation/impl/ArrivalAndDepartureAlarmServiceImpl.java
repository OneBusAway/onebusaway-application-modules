/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.impl;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.RegisterAlarmQueryBean;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.AlarmAction;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureAlarmService;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureQuery;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationListener;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ArrivalAndDepartureAlarmServiceImpl implements
    ArrivalAndDepartureAlarmService, BlockLocationListener {

  private static Logger _log = LoggerFactory.getLogger(ArrivalAndDepartureAlarmServiceImpl.class);

  private ArrivalAndDepartureService _arrivalAndDepartureService;

  private ConcurrentMap<BlockInstance, AlarmsForBlockInstance> _alarmsByBlockInstance = new ConcurrentHashMap<BlockInstance, AlarmsForBlockInstance>();

  private Map<AgencyAndId, AlarmForBlockInstance> _alarmsById = new HashMap<AgencyAndId, AlarmForBlockInstance>();

  private ScheduledExecutorService _executor;

  private int _threadPoolSize = 5;

  @Autowired
  public void setArrivalAndDepartureService(
      ArrivalAndDepartureService arrivalAndDepartureService) {
    _arrivalAndDepartureService = arrivalAndDepartureService;
  }

  public void setThreadPoolSize(int threadPoolSize) {
    _threadPoolSize = threadPoolSize;
  }

  /****
   * 
   ****/

  @PostConstruct
  public void start() {
    _executor = Executors.newScheduledThreadPool(_threadPoolSize);
  }

  @PreDestroy
  public void stop() {
    if (_executor != null) {
      _executor.shutdownNow();
      _executor = null;
    }
  }

  /****
   * {@link ArrivalAndDepartureAlarmService} Interface
   ****/

  @Override
  public AgencyAndId registerAlarmForArrivalAndDepartureAtStop(
      ArrivalAndDepartureQuery query, RegisterAlarmQueryBean alarmBean) {

    ArrivalAndDepartureInstance instance = _arrivalAndDepartureService.getArrivalAndDepartureForStop(query);

    if (instance == null)
      throw new ServiceException("no arrival-departure found");

    /**
     * We group alarms by block instance
     */
    BlockInstance blockInstance = instance.getBlockInstance();

    /**
     * Retrieve the alarms for the block instance
     */
    AlarmsForBlockInstance alarms = getAlarmsForBlockInstance(blockInstance);

    /**
     * The effective schedule time is the point in the transit vehicle's
     * schedule run time when the alarm should be fired
     */
    int effectiveScheduleTime = computeEffectiveScheduleTimeForAlarm(alarmBean,
        instance);

    /**
     * Create and register the alarm
     */
    AlarmAction action = new AlarmAction();
    action.setUrl(alarmBean.getUrl());

    AlarmForBlockInstance alarm = alarms.registerAlarm(action,
        effectiveScheduleTime, instance);

    _alarmsById.put(alarm.getId(), alarm);

    _log.debug("alarm created: {}", alarm.getId());

    return alarm.getId();
  }

  @Override
  public void cancelAlarmForArrivalAndDepartureAtStop(AgencyAndId alarmId) {
    _log.debug("cancelling alarm: {}", alarmId);
    AlarmForBlockInstance alarm = _alarmsById.get(alarmId);
    if (alarm != null)
      alarm.setCanceled();
  }

  /****
   * {@link BlockLocationListener} Interface
   ****/

  @Override
  public void handleBlockLocation(BlockLocation blockLocation) {

    if (blockLocation == null)
      return;

    /**
     * If we have new real-time info for a block, we need to update the alarms
     * attached to the block instance
     */
    BlockInstance blockInstance = blockLocation.getBlockInstance();
    AlarmsForBlockInstance alarms = _alarmsByBlockInstance.get(blockInstance);
    if (alarms != null)
      alarms.updateBlockLocation(blockLocation);
  }

  /****
   * Private Methods
   ****/

  /**
   * The effective schedule time is the point in the transit vehicle's schedule
   * run time when the alarm should be fired. It's determined by the scheduled
   * departure or arrival time, adjusted by the alarm offset time
   */
  private int computeEffectiveScheduleTimeForAlarm(
      RegisterAlarmQueryBean alarmBean, ArrivalAndDepartureInstance instance) {

    long scheduleTime = alarmBean.isOnArrival()
        ? instance.getScheduledArrivalTime()
        : instance.getScheduledDepartureTime();

    int effectiveScheduleTime = (int) ((scheduleTime - instance.getServiceDate()) / 1000);

    return effectiveScheduleTime - alarmBean.getAlarmTimeOffset();
  }

  /**
   * We group the alarms by their block instance, storing the alarms in a
   * ConcurrentMap keyed off the block instance. When all alarms for a block
   * instance have been fired, we'd like to be able to clean and remove the
   * alarms object. However, there is a possibility for a race condition when
   * attempting to remove the AlarmsForBlockInstance object from the concurrent
   * map when another alarm is being registered at the same time. We get around
   * this by marking an alarms object as "canceled", indicating that no new
   * alarms can be registered. Thus, we loop while until we get an active alarm
   * instance.
   * 
   * @param blockInstance
   * @return
   */
  private AlarmsForBlockInstance getAlarmsForBlockInstance(
      BlockInstance blockInstance) {

    while (true) {
      AlarmsForBlockInstance alarms = _alarmsByBlockInstance.get(blockInstance);

      if (alarms == null) {
        AlarmsForBlockInstance newAlarms = new AlarmsForBlockInstance(
            blockInstance);
        alarms = _alarmsByBlockInstance.putIfAbsent(blockInstance, newAlarms);
        if (alarms == null)
          alarms = newAlarms;
      }

      if (alarms.isCanceled())
        continue;

      return alarms;
    }
  }

  private void fireAlarm(AlarmForBlockInstance alarm) {
    _executor.submit(new FireAlarmTask(alarm.getId(), alarm.action));
  }

  /****
   * 
   ****/

  private class AlarmsForBlockInstance implements Runnable {

    private final BlockInstance _blockInstance;

    /**
     * Queue of alarms where no real-time data is available. If real-time
     * becomes available, we'll upgrade the alarm to the first real-time-equiped
     * vehicle.
     */
    private PriorityQueue<AlarmForBlockInstance> _noVehicleIdQueue = new PriorityQueue<AlarmForBlockInstance>();

    /**
     * Queues of alarms grouped by vehicle id. Remember that multiple vehicles
     * can be servicing the same block instance.
     */
    private Map<AgencyAndId, VehicleInfo> _vehicleInfoByVehicleId = new HashMap<AgencyAndId, VehicleInfo>();

    /**
     * The actual task that will run to check and fire alarms in the future. We
     * reschedule this task to reflect the next upcoming alarm.
     */
    private Future<?> _alarmTask = null;

    /**
     * Indicates that this alarms instance has been canceled and no new alarms
     * should be registered. An alarm instance is canceled when it contains no
     * new alarms. The "canceled" flag helps avoid a race condition where
     * additional alarms are added while we are in the process of cleanup.
     */
    private boolean _canceled = false;

    public AlarmsForBlockInstance(BlockInstance blockInstance) {
      _blockInstance = blockInstance;
    }

    public synchronized boolean isCanceled() {
      return _canceled;
    }

    public synchronized AlarmForBlockInstance registerAlarm(AlarmAction action,
        int effectiveScheduleTime, ArrivalAndDepartureInstance instance) {

      StopEntry stop = instance.getStop();
      AgencyAndId stopId = stop.getId();
      AgencyAndId alarmId = new AgencyAndId(stopId.getAgencyId(),
          UUID.randomUUID().toString());

      AlarmForBlockInstance alarm = new AlarmForBlockInstance(alarmId, action,
          effectiveScheduleTime);

      /**
       * We put the alarm in the schedule-only vs real-time queue as appropriate
       */
      BlockLocation blockLocation = instance.getBlockLocation();

      if (blockLocation == null || blockLocation.getVehicleId() == null) {

        _log.debug("schedule only for alarm: {}", instance);
        _noVehicleIdQueue.add(alarm);

      } else {

        _log.debug("real-time for alarm: {}", instance);
        AgencyAndId vehicleId = blockLocation.getVehicleId();
        VehicleInfo vehicleInfo = getVehicleInfoForVehicleId(vehicleId, true);
        if (blockLocation.isScheduleDeviationSet())
          vehicleInfo.setScheduleDeviation((int) blockLocation.getScheduleDeviation());
        else
          _log.warn("no schedule deviation for block location " + blockLocation);
        PriorityQueue<AlarmForBlockInstance> queue = vehicleInfo.getQueue();
        queue.add(alarm);
      }

      processQueues();

      return alarm;
    }

    public synchronized void updateBlockLocation(BlockLocation blockLocation) {

      AgencyAndId vehicleId = blockLocation.getVehicleId();

      if (vehicleId == null) {
        _log.warn("expected a vehicle id with block location" + blockLocation);
        return;
      }

      if (!blockLocation.isScheduleDeviationSet()) {
        _log.warn("expected schedule deviation with block location"
            + blockLocation);
      }

      _log.debug("updating block location for vehicle: {}",
          blockLocation.getVehicleId());

      /**
       * We've create the vehicle info queue if it means we can move alarms out
       * of the "scheduled arrival" queue
       */
      boolean create = !_noVehicleIdQueue.isEmpty();

      VehicleInfo vehicleInfo = getVehicleInfoForVehicleId(vehicleId, create);

      if (vehicleInfo == null)
        return;

      vehicleInfo.setScheduleDeviation((int) blockLocation.getScheduleDeviation());

      moveNoVehicleAlarmsToVehicleAlarms();
      processQueues();
    }

    /**
     * This is called by the scheduler
     */
    @Override
    public synchronized void run() {
      _alarmTask = null;
      processQueues();
    }

    /****
     * 
     ****/

    private VehicleInfo getVehicleInfoForVehicleId(AgencyAndId vehicleId,
        boolean create) {

      VehicleInfo vehicleInfo = _vehicleInfoByVehicleId.get(vehicleId);

      if (vehicleInfo == null && create) {
        vehicleInfo = new VehicleInfo();
        _vehicleInfoByVehicleId.put(vehicleId, vehicleInfo);
      }

      return vehicleInfo;
    }

    /**
     * If we had alarms set for a "scheduled arrival" and we now have real-time
     * tracking for a vehicle serving that arrival, we move the alarms over.
     */
    private void moveNoVehicleAlarmsToVehicleAlarms() {

      if (_noVehicleIdQueue.isEmpty() || _vehicleInfoByVehicleId.isEmpty())
        return;
      VehicleInfo first = _vehicleInfoByVehicleId.values().iterator().next();

      PriorityQueue<AlarmForBlockInstance> queue = first.getQueue();

      queue.addAll(_noVehicleIdQueue);
      _noVehicleIdQueue.clear();
    }

    private void processQueues() {

      if (_alarmTask != null)
        _alarmTask.cancel(false);

      boolean allQueuesAreEmpty = true;
      int minNextAlarmTime = Integer.MAX_VALUE;

      for (VehicleInfo vehicleInfo : _vehicleInfoByVehicleId.values()) {
        PriorityQueue<AlarmForBlockInstance> queue = vehicleInfo.getQueue();
        int scheduleDeviation = vehicleInfo.getScheduleDeviation();
        int nextAlarmTime = processQueue(queue, scheduleDeviation);
        if (nextAlarmTime > 0) {
          minNextAlarmTime = Math.min(minNextAlarmTime, nextAlarmTime);
          allQueuesAreEmpty = false;
        }
      }

      int nextAlarmTime = processQueue(_noVehicleIdQueue, 0);
      if (nextAlarmTime > 0) {
        minNextAlarmTime = Math.min(minNextAlarmTime, nextAlarmTime);
        allQueuesAreEmpty = false;
      }

      if (allQueuesAreEmpty) {
        _log.debug("all alarm queues are empty, cleaning up: {}",
            _blockInstance);
        _vehicleInfoByVehicleId.clear();
        _canceled = true;
        _alarmsByBlockInstance.remove(_blockInstance);

      } else {
        _log.debug("scheduling next alarm check in {} secs for {}",
            minNextAlarmTime, _blockInstance);
        /**
         * Schedule the next alarm
         */
        _alarmTask = _executor.schedule(this, minNextAlarmTime,
            TimeUnit.SECONDS);
      }
    }

    private int processQueue(PriorityQueue<AlarmForBlockInstance> queue,
        int scheduleDeviation) {

      int effectiveScheduleTime = (int) ((SystemTime.currentTimeMillis() - _blockInstance.getServiceDate()) / 1000 - scheduleDeviation);

      while (!queue.isEmpty()) {
        AlarmForBlockInstance alarm = queue.peek();

        if (alarm.isCanceled()) {
          queue.poll();
          continue;
        }

        /**
         * If the first alarm in the queue isn't ready to be fired yet, we
         * return the time until it should be fired
         */
        if (effectiveScheduleTime < alarm.getEffectiveScheduleTime()) {
          return alarm.getEffectiveScheduleTime() - effectiveScheduleTime;
        }
        queue.poll();
        fireAlarm(alarm);
      }

      /**
       * We've gone through all the alarms
       */
      return -1;
    }

  }

  private class VehicleInfo {

    private final PriorityQueue<AlarmForBlockInstance> _queue = new PriorityQueue<AlarmForBlockInstance>();

    private int _scheduleDeviation = 0;

    public int getScheduleDeviation() {
      return _scheduleDeviation;
    }

    public void setScheduleDeviation(int scheduleDeviation) {
      _scheduleDeviation = scheduleDeviation;
    }

    public PriorityQueue<AlarmForBlockInstance> getQueue() {
      return _queue;
    }
  }

  private class AlarmForBlockInstance implements
      Comparable<AlarmForBlockInstance> {

    private final AgencyAndId id;

    private final AlarmAction action;

    private final int effectiveScheduleTime;

    private boolean canceled = false;

    public AlarmForBlockInstance(AgencyAndId id, AlarmAction action,
        int effectiveScheduleTime) {
      this.id = id;
      this.action = action;
      this.effectiveScheduleTime = effectiveScheduleTime;
    }

    public AgencyAndId getId() {
      return id;
    }

    public int getEffectiveScheduleTime() {
      return effectiveScheduleTime;
    }

    public void setCanceled() {
      canceled = true;
    }

    public boolean isCanceled() {
      return canceled;
    }

    @Override
    public int compareTo(AlarmForBlockInstance o) {
      return this.effectiveScheduleTime - o.effectiveScheduleTime;
    }
  }

  /**
   * This task encapsulates the task of actually executing an alarm so that it
   * can be executed asynchronously
   * 
   * @author bdferris
   * 
   */
  private static class FireAlarmTask implements Runnable {

    private final AgencyAndId alarmId;

    private final AlarmAction action;

    public FireAlarmTask(AgencyAndId alarmId, AlarmAction action) {
      this.alarmId = alarmId;
      this.action = action;
    }

    @Override
    public void run() {
      try {
        String rawUrl = action.getUrl();
        String rawAlarmId = AgencyAndIdLibrary.convertToString(alarmId);
        rawUrl = rawUrl.replace("#ALARM_ID#", rawAlarmId);
        URL url = new URL(rawUrl);
        URLConnection connection = url.openConnection();
        InputStream in = connection.getInputStream();
        in.close();
      } catch (Throwable ex) {
        _log.warn("error firing alarm", ex);
      }
    }
  }

}
