/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
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
package org.onebusaway.transit_data_federation.impl.beans;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.ArrivalsAndDeparturesQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.realtime.HistogramBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.impl.realtime.apc.VehicleOccupancyRecordCache;
import org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeNegativeArrivals;
import org.onebusaway.transit_data_federation.model.TargetTime;
import org.onebusaway.transit_data_federation.model.bundle.HistoricalRidership;
import org.onebusaway.transit_data_federation.model.narrative.StopTimeNarrative;
import org.onebusaway.transit_data_federation.services.*;
import org.onebusaway.transit_data_federation.services.beans.ArrivalsAndDeparturesBeanService;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.narrative.NarrativeService;
import org.onebusaway.transit_data_federation.services.realtime.ArrivalAndDepartureInstance;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.RealTimeHistoryService;
import org.onebusaway.transit_data_federation.services.realtime.ScheduleDeviationHistogram;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ManagedResource("org.onebusaway.transit_data_federation.impl.beans:name=ArrivalsAndDeparturesBeanServiceImpl")
public class ArrivalsAndDeparturesBeanServiceImpl implements
    ArrivalsAndDeparturesBeanService {

  private TransitGraphDao _transitGraphDao;

  private ArrivalAndDepartureService _arrivalAndDepartureService;

  private NarrativeService _narrativeService;

  private TripBeanService _tripBeanService;

  private StopBeanService _stopBeanService;

  private TripDetailsBeanService _tripDetailsBeanService;

  private ServiceAlertsBeanService _serviceAlertsBeanService;

  private RealTimeHistoryService _realTimeHistoryService;
  
  private GtfsRealtimeNegativeArrivals _gtfsRealtimeNegativeArrivals;

  private RidershipService _ridershipService;

  private VehicleOccupancyRecordCache _vehicleOccupancyRecordCache;
  
  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setArrivalAndDepartureService(
      ArrivalAndDepartureService arrivalAndDepartureService) {
    _arrivalAndDepartureService = arrivalAndDepartureService;
  }

  @Autowired
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Autowired
  public void setRidershipService(RidershipService ridershipService) { _ridershipService = ridershipService; }

  @Autowired
  public void setVehicleOccupancyRecordCache(VehicleOccupancyRecordCache cache) { _vehicleOccupancyRecordCache = cache; }

  @Autowired
  public void setTripDetailsBeanService(
      TripDetailsBeanService tripDetailsBeanService) {
    _tripDetailsBeanService = tripDetailsBeanService;
  }

  @Autowired
  public void setServiceAlertsBeanService(
      ServiceAlertsBeanService serviceAlertsBeanService) {
    _serviceAlertsBeanService = serviceAlertsBeanService;
  }

  @Autowired
  public void setRealTimeHistoryService(
      RealTimeHistoryService realTimeHistoryService) {
    _realTimeHistoryService = realTimeHistoryService;;
  }
  
  @Autowired
  public void setGtfsRealtimeNegativeArrivals(
      GtfsRealtimeNegativeArrivals _gtfsRealtimeNegativeArrivals) {
    this._gtfsRealtimeNegativeArrivals = _gtfsRealtimeNegativeArrivals;
  }

  private AtomicInteger _stopTimesTotal = new AtomicInteger();

  private AtomicInteger _stopTimesWithPredictions = new AtomicInteger();

  @ManagedAttribute()
  public int getStopTimesTotal() {
    return _stopTimesTotal.intValue();
  }

  @ManagedAttribute
  public int getStopTimesWithPredictions() {
    return _stopTimesWithPredictions.intValue();
  }
  
  private boolean useScheduleDeviationHistory = true;
  /**
   * Optionally turn off schedule deviation history support.
   * @param useScheduleDeviationHistory flag to use/disable deviation history
   */
  public void setScheduleDeviationHistory(boolean useScheduleDeviationHistory) {
    this.useScheduleDeviationHistory = useScheduleDeviationHistory;
  }

  /****
   * {@link ArrivalsAndDeparturesBeanService} Interface
   ****/

  @Override
  public List<ArrivalAndDepartureBean> getArrivalsAndDeparturesByStopId(
      AgencyAndId stopId, ArrivalsAndDeparturesQueryBean query) {

    StopEntry stop = _transitGraphDao.getStopEntryForId(stopId, true);
    
    long time = query.getTime();

    int minutesBefore = Math.max(query.getMinutesBefore(),
        query.getFrequencyMinutesBefore());
    int minutesAfter = Math.max(query.getMinutesAfter(),
        query.getFrequencyMinutesAfter());

    long fromTime = time - minutesBefore * 60 * 1000;
    long toTime = time + minutesAfter * 60 * 1000;

    long nonFrequencyFromTime = time - query.getMinutesBefore() * 60 * 1000;
    long nonFrequencyToTime = time + query.getMinutesAfter() * 60 * 1000;

    long frequencyFromTime = time - query.getFrequencyMinutesBefore() * 60
        * 1000;
    long frequencyToTime = time + query.getFrequencyMinutesAfter() * 60 * 1000;

    TargetTime target = new TargetTime(time, time);

    List<ArrivalAndDepartureInstance> instances = _arrivalAndDepartureService.getArrivalsAndDeparturesForStopInTimeRange(
        stop, target, fromTime, toTime);

    List<ArrivalAndDepartureBean> beans = new ArrayList<ArrivalAndDepartureBean>();

    Map<AgencyAndId, StopBean> stopBeanCache = new HashMap<AgencyAndId, StopBean>();

    for (ArrivalAndDepartureInstance instance : instances) {

      FrequencyEntry frequency = instance.getFrequency();

      long from = frequency != null ? frequencyFromTime : nonFrequencyFromTime;
      long to = frequency != null ? frequencyToTime : nonFrequencyToTime;

      if (!isArrivalAndDepartureInRange(instance, from, to))
        continue;
      
      ArrivalAndDepartureBean bean = getStopTimeInstanceAsBean(time, instance,
          stopBeanCache);
      applyBlockLocationToBean(instance, bean, time);
      
      Boolean isNegativeScheduledArrivalsEnabled = _gtfsRealtimeNegativeArrivals.getShowNegativeScheduledArrivalByAgencyId(
          instance.getBlockTrip().getTrip().getId().getAgencyId());
      
      if(isNegativeScheduledArrivalsEnabled != null && !isNegativeScheduledArrivalsEnabled 
          && bean.getNumberOfStopsAway() < 0 && bean.getPredictedArrivalTime() <= 0)
        continue;
      
      applySituationsToBean(time, instance, bean);

      beans.add(bean);
    }

    Collections.sort(beans, new ArrivalAndDepartureComparator());

    return beans;
  }

  @Override
  public ArrivalAndDepartureBean getArrivalAndDepartureForStop(
      ArrivalAndDepartureQuery query) {

    long time = query.getTime();

    ArrivalAndDepartureInstance instance = _arrivalAndDepartureService.getArrivalAndDepartureForStop(query);
    if (instance == null) {
      return null;
    }

    ArrivalAndDepartureBean bean = getStopTimeInstanceAsBean(time, instance,
        new HashMap<AgencyAndId, StopBean>());
    applyBlockLocationToBean(instance, bean, time);
    applySituationsToBean(time, instance, bean);

    if (!this.useScheduleDeviationHistory) {
      return bean;
    }

    int step = 120;

    ScheduleDeviationHistogram histo = _realTimeHistoryService.getScheduleDeviationHistogramForArrivalAndDepartureInstance(
        instance, step);

    if (histo != null) {

      int[] sds = histo.getScheduleDeviations();

      double[] values = new double[sds.length];
      String[] labels = new String[sds.length];
      for (int i = 0; i < sds.length; i++) {
        int sd = sds[i];
        values[i] = sd;
        labels[i] = Integer.toString(sd / 60);
      }

      HistogramBean hb = new HistogramBean();
      hb.setValues(values);
      hb.setCounts(histo.getCounts());
      hb.setLabels(labels);
      bean.setScheduleDeviationHistogram(hb);
    }

    return bean;
  }

  /****
   * Private Methods
   ****/

  private ArrivalAndDepartureBean getStopTimeInstanceAsBean(long time,
      ArrivalAndDepartureInstance instance,
      Map<AgencyAndId, StopBean> stopBeanCache) {

    ArrivalAndDepartureBean pab = new ArrivalAndDepartureBean();

    pab.setServiceDate(instance.getServiceDate());

    BlockStopTimeEntry blockStopTime = instance.getBlockStopTime();
    BlockTripEntry blockTrip = blockStopTime.getTrip();
    StopTimeEntry stopTime = blockStopTime.getStopTime();
    StopEntry stop = stopTime.getStop();
    TripEntry trip = stopTime.getTrip();

    TripBean tripBean = _tripBeanService.getTripForId(trip.getId());
    pab.setTrip(tripBean);
    pab.setBlockTripSequence(blockTrip.getSequence());

    pab.setArrivalEnabled(stopTime.getSequence() > 0);
    pab.setDepartureEnabled(stopTime.getSequence() + 1 < trip.getStopTimes().size());
    
    StopTimeNarrative stopTimeNarrative = _narrativeService.getStopTimeForEntry(stopTime);
    pab.setRouteShortName(stopTimeNarrative.getRouteShortName());
    pab.setTripHeadsign(stopTimeNarrative.getStopHeadsign());

    StopBean stopBean = stopBeanCache.get(stop.getId());

    if (stopBean == null) {
      stopBean = _stopBeanService.getStopForId(stop.getId(), null);
      stopBeanCache.put(stop.getId(), stopBean);
    }

    pab.setStop(stopBean);
    pab.setStopSequence(stopTime.getSequence());
    pab.setTotalStopsInTrip(stopTime.getTotalStopsInTrip());
    
    pab.setStatus("default");

    pab.setScheduledArrivalTime(instance.getScheduledArrivalTime());
    pab.setScheduledDepartureTime(instance.getScheduledDepartureTime());

    FrequencyEntry frequency = instance.getFrequencyLabel();
    pab.setFrequency(null);
    if (frequency != null) {
      FrequencyBean fb = FrequencyBeanLibrary.getBeanForFrequency(
          instance.getServiceDate(), frequency);
      pab.setFrequency(fb);
    }

    if (_ridershipService != null) {
      List<HistoricalRidership> occ = _ridershipService.getHistoricalRiderships(trip.getRoute().getId(), trip.getId(), stop.getId(), pab.getServiceDate());
      if(occ != null && occ.size() > 0) pab.setHistoricalOccupancy(OccupancyStatus.toEnum(occ.get(0).getLoadFactor()));
    }
    return pab;
  }

  private void applyBlockLocationToBean(ArrivalAndDepartureInstance instance,
      ArrivalAndDepartureBean bean, long targetTime) {

    boolean hasFrequency = instance.getFrequency() != null;

    if (instance.isPredictedArrivalTimeSet()) {
      bean.setPredictedArrivalTime(instance.getPredictedArrivalTime());
      if (hasFrequency)
        bean.setScheduledArrivalTime(bean.getPredictedArrivalTime());
    }

    if (instance.isPredictedDepartureTimeSet()) {
      bean.setPredictedDepartureTime(instance.getPredictedDepartureTime());
      if (hasFrequency)
        bean.setScheduledDepartureTime(bean.getPredictedDepartureTime());
    }

    BlockStopTimeEntry stopTime = instance.getBlockStopTime();
    BlockLocation blockLocation = instance.getBlockLocation();

    if (blockLocation == null)
      return;

    bean.setPredicted(blockLocation.isPredicted());

    // Distance from stop
    if (blockLocation.isDistanceAlongBlockSet()) {
      double distanceFromStop = stopTime.getDistanceAlongBlock()
          - blockLocation.getDistanceAlongBlock();
      bean.setDistanceFromStop(distanceFromStop);
    } else {
      double distanceFromStop = stopTime.getDistanceAlongBlock()
          - blockLocation.getScheduledDistanceAlongBlock();
      bean.setDistanceFromStop(distanceFromStop);
    }

    // Number of stops away
    if (blockLocation.getNextStop() != null) {
      BlockStopTimeEntry nextStopTime = blockLocation.getNextStop();
      bean.setNumberOfStopsAway(stopTime.getBlockSequence()
          - nextStopTime.getBlockSequence());
    }

    if (blockLocation.getLastUpdateTime() > 0)
      bean.setLastUpdateTime(blockLocation.getLastUpdateTime());

    if (blockLocation.getVehicleId() != null) {
      bean.setVehicleId(AgencyAndIdLibrary.convertToString(blockLocation.getVehicleId()));
      if (_vehicleOccupancyRecordCache != null && blockLocation.getActiveTrip() != null) {
        // be specific in our vehicle lookup -- we only want to apply occupancy if its the same route/direction
        VehicleOccupancyRecord vor = _vehicleOccupancyRecordCache.getRecordForVehicleIdAndRoute(AgencyAndIdLibrary.convertFromString(bean.getVehicleId()),
                blockLocation.getActiveTrip().getTrip().getRoute().getId().toString(),
                blockLocation.getActiveTrip().getTrip().getDirectionId());
        if (vor != null)
          bean.setOccupancyStatus(vor.getOccupancyStatus());
      }


    }

    TripStatusBean tripStatusBean = _tripDetailsBeanService.getBlockLocationAsStatusBean(
        blockLocation, targetTime);
    bean.setTripStatus(tripStatusBean);
  }

  private void applySituationsToBean(long time,
      ArrivalAndDepartureInstance instance, ArrivalAndDepartureBean bean) {

    BlockInstance blockInstance = instance.getBlockInstance();

    AgencyAndId vehicleId = null;
    BlockLocation blockLocation = instance.getBlockLocation();
    if (blockLocation != null)
      vehicleId = blockLocation.getVehicleId();

    List<ServiceAlertBean> situations = _serviceAlertsBeanService.getServiceAlertsForStopCall(
        time, blockInstance, instance.getBlockStopTime(), vehicleId);

    if (!situations.isEmpty())
      bean.setSituations(situations);
  }

  private boolean isArrivalAndDepartureInRange(
      ArrivalAndDepartureInstance instance, long timeFrom, long timeTo) {

    if (timeFrom <= instance.getScheduledArrivalTime()
        && instance.getScheduledArrivalTime() <= timeTo)
      return true;

    if (timeFrom <= instance.getScheduledDepartureTime()
        && instance.getScheduledDepartureTime() <= timeTo)
      return true;

    if (instance.isPredictedArrivalTimeSet()
        && timeFrom <= instance.getPredictedArrivalTime()
        && instance.getPredictedArrivalTime() <= timeTo)
      return true;

    if (instance.isPredictedDepartureTimeSet()
        && timeFrom <= instance.getPredictedDepartureTime()
        && instance.getPredictedDepartureTime() <= timeTo)
      return true;

    return false;
  }
  
  private static class ArrivalAndDepartureComparator implements
      Comparator<ArrivalAndDepartureBean> {

    public int compare(ArrivalAndDepartureBean o1, ArrivalAndDepartureBean o2) {
      long t1 = o1.getScheduledArrivalTime();
      if (o1.hasPredictedArrivalTime())
        t1 = o1.getPredictedArrivalTime();
      long t2 = o2.getScheduledArrivalTime();
      if (o2.hasPredictedArrivalTime())
        t2 = o2.getPredictedArrivalTime();
      return (int) (t1 - t2);
    }
  }

}
