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
import org.onebusaway.transit_data_federation.services.AlarmAction;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureAlarmService;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureQuery;
import org.onebusaway.transit_data_federation.services.ArrivalAndDepartureService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocationListener;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
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

    BlockInstance blockInstance = instance.getBlockInstance();

    AlarmsForBlockInstance alarms = getAlarmsForBlockInstance(blockInstance);

    int effectiveScheduleTime = computeEffectiveScheduleTimeForAlarm(alarmBean,
        instance);

    AlarmAction action = new AlarmAction();
    action.setUrl(alarmBean.getUrl());

    AlarmForBlockInstance alarm = alarms.registerAlarm(action,
        effectiveScheduleTime, instance);

    _alarmsById.put(alarm.getId(), alarm);

    return alarm.getId();
  }

  @Override
  public void cancelAlarmForArrivalAndDepartureAtStop(AgencyAndId alarmId) {
    AlarmForBlockInstance alarm = _alarmsById.get(alarmId);
    if (alarm != null)
      alarm.setCanceled();
  }

  /****
   * {@link BlockLocationListener} Interface
   ****/

  @Override
  public void handleBlockLocation(BlockLocation blockLocation) {
    if( blockLocation == null)
      return;
    BlockInstance blockInstance = blockLocation.getBlockInstance();
    AlarmsForBlockInstance alarms = _alarmsByBlockInstance.get(blockInstance);
    if (alarms != null)
      alarms.updateBlockLocation(blockLocation);
  }

  /****
   * Private Methods
   ****/

  private int computeEffectiveScheduleTimeForAlarm(
      RegisterAlarmQueryBean alarmBean, ArrivalAndDepartureInstance instance) {

    long scheduleTime = alarmBean.isOnArrival()
        ? instance.getScheduledArrivalTime()
        : instance.getScheduledDepartureTime();

    int effectiveScheduleTime = (int) ((scheduleTime - instance.getServiceDate()) / 1000);

    return effectiveScheduleTime - alarmBean.getAlarmTimeOffset();
  }

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
    _executor.submit(new FireAlarmTask(alarm.action));
  }

  /****
   * 
   ****/

  private class AlarmsForBlockInstance implements Runnable {

    private final BlockInstance _blockInstance;

    private PriorityQueue<AlarmForBlockInstance> _noVehicleIdQueue = new PriorityQueue<AlarmForBlockInstance>();

    private Map<AgencyAndId, VehicleInfo> _vehicleInfoByVehicleId = new HashMap<AgencyAndId, VehicleInfo>();

    private Future<?> _alarmTask = null;

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

      BlockLocation blockLocation = instance.getBlockLocation();
      AgencyAndId vehicleId = blockLocation.getVehicleId();

      if (vehicleId == null) {
        _noVehicleIdQueue.add(alarm);
      } else {
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

        _vehicleInfoByVehicleId.clear();
        _canceled = true;
        _alarmsByBlockInstance.remove(_blockInstance);

      } else {
        /**
         * Schedule the next alarm
         */
        _alarmTask = _executor.schedule(this, minNextAlarmTime,
            TimeUnit.SECONDS);
      }
    }

    private int processQueue(PriorityQueue<AlarmForBlockInstance> queue,
        int scheduleDeviation) {

      int effectiveScheduleTime = (int) ((System.currentTimeMillis() - _blockInstance.getServiceDate()) / 1000 - scheduleDeviation);

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

    private final AlarmAction action;

    public FireAlarmTask(AlarmAction action) {
      this.action = action;
    }

    @Override
    public void run() {
      try {
        URL url = new URL(action.getUrl());
        URLConnection connection = url.openConnection();
        InputStream in = connection.getInputStream();
        in.close();
      } catch (Throwable ex) {
        _log.warn("error firing alarm", ex);
      }
    }
  }

}
