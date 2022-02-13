/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
 * Copyright (C) 2015 University of South Florida
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.TripStopTimesBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.trips.TimepointPredictionBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.model.trips.TripsForAgencyQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForBoundsQueryBean;
import org.onebusaway.transit_data.model.trips.TripsForRouteQueryBean;
import org.onebusaway.transit_data_federation.impl.realtime.apc.VehicleOccupancyRecordCache;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.ServiceAlertsBeanService;
import org.onebusaway.transit_data_federation.services.beans.StopBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.beans.TripStopTimesBeanService;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockTripInstanceLibrary;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.FrequencyEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TransitGraphDao;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TripStatusBeanServiceImpl implements TripDetailsBeanService {

  private static Logger _log = LoggerFactory.getLogger(TripStatusBeanServiceImpl.class);

  private TransitGraphDao _transitGraphDao;

  private BlockStatusService _blockStatusService;

  private TripBeanService _tripBeanService;

  private TripStopTimesBeanService _tripStopTimesBeanService;

  private StopBeanService _stopBeanService;

  private ServiceAlertsBeanService _serviceAlertBeanService;

  private VehicleOccupancyRecordCache _vehicleOccupancyRecordCache;

  @Autowired
  public void setTransitGraphDao(TransitGraphDao transitGraphDao) {
    _transitGraphDao = transitGraphDao;
  }

  @Autowired
  public void setBlockStatusService(BlockStatusService blockStatusService) {
    _blockStatusService = blockStatusService;
  }

  @Autowired
  public void setTripBeanService(TripBeanService tripBeanService) {
    _tripBeanService = tripBeanService;
  }

  @Autowired
  public void setTripStopTimesBeanService(
      TripStopTimesBeanService tripStopTimesBeanService) {
    _tripStopTimesBeanService = tripStopTimesBeanService;
  }

  @Autowired
  public void setStopBeanService(StopBeanService stopBeanService) {
    _stopBeanService = stopBeanService;
  }

  @Autowired
  public void setServiceAlertBeanService(
      ServiceAlertsBeanService serviceAlertBeanService) {
    _serviceAlertBeanService = serviceAlertBeanService;
  }

  @Autowired
  public void setVehicleOccupancyRecordCache(VehicleOccupancyRecordCache cache) {
    this._vehicleOccupancyRecordCache = cache;
  }
  /****
   * {@link TripDetailsBeanService} Interface
   ****/

  @Override
  public TripDetailsBean getTripForId(TripDetailsQueryBean query) {

    ListBean<TripDetailsBean> listBean = getTripsForId(query);
    List<TripDetailsBean> trips = listBean.getList();

    if (trips == null || trips.isEmpty()) {
      return null;
    } else if (trips.size() == 1) {
      return trips.get(0);
    } else {
      // Be smarter here?
      return trips.get(0);
    }
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForId(TripDetailsQueryBean query) {

    AgencyAndId tripId = AgencyAndIdLibrary.convertFromString(query.getTripId());
    long serviceDate = query.getServiceDate();
    AgencyAndId vehicleId = AgencyAndIdLibrary.convertFromString(query.getVehicleId());
    long time = query.getTime();

    TripEntry tripEntry = _transitGraphDao.getTripEntryForId(tripId);
    if (tripEntry == null)
      return new ListBean<TripDetailsBean>();

    Map<BlockInstance, List<BlockLocation>> locationsByInstance = _blockStatusService.getBlocks(
        tripEntry.getBlock().getId(), serviceDate, vehicleId, time);

    List<TripDetailsBean> tripDetails = new ArrayList<TripDetailsBean>();

    for (Map.Entry<BlockInstance, List<BlockLocation>> entry : locationsByInstance.entrySet()) {

      BlockInstance blockInstance = entry.getKey();
      List<BlockLocation> locations = entry.getValue();

      BlockTripInstance blockTripInstance = BlockTripInstanceLibrary.getBlockTripInstance(
          blockInstance, tripId);

      if (blockTripInstance == null)
        throw new IllegalStateException("expected blockTrip for trip="
            + tripEntry + " and block=" + blockInstance);

      /**
       * If we have no locations for the specified block instance, it means the
       * block is not currently active. But we can still attempt to construct a
       * trip details
       */
      if (locations.isEmpty()) {
        TripDetailsBean details = getTripEntryAndBlockLocationAsTripDetails(
            blockTripInstance, null, query.getInclusion(), time);
        tripDetails.add(details);
      } else {
        for (BlockLocation location : locations) {
          TripDetailsBean details = getBlockLocationAsTripDetails(
              blockTripInstance, location, query.getInclusion(), time);
          tripDetails.add(details);
        }
      }
    }
    return new ListBean<TripDetailsBean>(tripDetails, false);
  }

  @Override
  public TripDetailsBean getTripForVehicle(AgencyAndId vehicleId, long time,
      TripDetailsInclusionBean inclusion) {

    BlockLocation blockLocation = _blockStatusService.getBlockForVehicle(
        vehicleId, time);
    if (blockLocation == null)
      return null;
    return getBlockLocationAsTripDetails(blockLocation.getActiveTripInstance(),
        blockLocation, inclusion, time);
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForBounds(
      TripsForBoundsQueryBean query) {
    List<BlockLocation> locations = _blockStatusService.getBlocksForBounds(
        query.getBounds(), query.getTime());
    return getBlockLocationsAsTripDetails(locations, query.getInclusion(),
        query.getTime());
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForRoute(TripsForRouteQueryBean query) {
    AgencyAndId routeId = AgencyAndIdLibrary.convertFromString(query.getRouteId());
    List<BlockLocation> locations = _blockStatusService.getBlocksForRoute(
        routeId, query.getTime());
    return getBlockLocationsAsTripDetails(locations, query.getInclusion(),
        query.getTime());
  }

  @Override
  public ListBean<TripDetailsBean> getTripsForAgency(
      TripsForAgencyQueryBean query) {
    List<BlockLocation> locations = _blockStatusService.getActiveBlocksForAgency(
        query.getAgencyId(), query.getTime());
    return getBlockLocationsAsTripDetails(locations, query.getInclusion(),
        query.getTime());
  }

  @Override
  public TripStatusBean getBlockLocationAsStatusBean(
      BlockLocation blockLocation, long time) {

    TripStatusBean bean = new TripStatusBean();
    bean.setStatus("default");

    BlockInstance blockInstance = blockLocation.getBlockInstance();
    long serviceDate = blockInstance.getServiceDate();

    bean.setServiceDate(serviceDate);

    bean.setLastUpdateTime(blockLocation.getLastUpdateTime());
    bean.setLastLocationUpdateTime(blockLocation.getLastLocationUpdateTime());

    bean.setLastKnownLocation(blockLocation.getLastKnownLocation());
    bean.setLastKnownOrientation(blockLocation.getLastKnownOrientation());

    bean.setLocation(blockLocation.getLocation());
    bean.setOrientation(blockLocation.getOrientation());

    bean.setLastKnownLocation(blockLocation.getLastKnownLocation());
    if (blockLocation.isLastKnownOrientationSet())
      bean.setLastKnownOrientation(blockLocation.getLastKnownOrientation());

    bean.setScheduleDeviation(blockLocation.getScheduleDeviation());

    BlockTripInstance activeTripInstance = blockLocation.getActiveTripInstance();

    if (activeTripInstance != null) {
      BlockTripEntry activeBlockTrip = activeTripInstance.getBlockTrip();
      bean.setScheduledDistanceAlongTrip(blockLocation.getScheduledDistanceAlongBlock()
          - activeBlockTrip.getDistanceAlongBlock());
      bean.setDistanceAlongTrip(blockLocation.getDistanceAlongBlock()
          - activeBlockTrip.getDistanceAlongBlock());
      TripEntry activeTrip = activeBlockTrip.getTrip();
      bean.setTotalDistanceAlongTrip(activeTrip.getTotalTripDistance());

      TripBean activeTripBean = _tripBeanService.getTripForId(activeTrip.getId());
      bean.setActiveTrip(activeTripBean);
      bean.setBlockTripSequence(activeBlockTrip.getSequence());

      if (blockLocation.isLastKnownDistanceAlongBlockSet()) {
        bean.setLastKnownDistanceAlongTrip(blockLocation.getLastKnownDistanceAlongBlock()
            - activeBlockTrip.getDistanceAlongBlock());
      }

      FrequencyEntry frequencyLabel = activeTripInstance.getFrequencyLabel();

      if (frequencyLabel != null) {
        FrequencyBean fb = FrequencyBeanLibrary.getBeanForFrequency(
            serviceDate, frequencyLabel);
        bean.setFrequency(fb);
      }

    } else {
      _log.warn("no active block trip for block location: blockInstance="
          + blockLocation.getBlockInstance() + " time=" + time);
    }

    BlockStopTimeEntry closestStop = blockLocation.getClosestStop();
    if (closestStop != null) {
      StopTimeEntry stopTime = closestStop.getStopTime();
      StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId(), null);
      bean.setClosestStop(stopBean);
      bean.setClosestStopTimeOffset(blockLocation.getClosestStopTimeOffset());
    }

    BlockStopTimeEntry nextStop = blockLocation.getNextStop();
    if (nextStop != null) {
      StopTimeEntry stopTime = nextStop.getStopTime();
      StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId(), null);
      bean.setNextStop(stopBean);
      bean.setNextStopTimeOffset(blockLocation.getNextStopTimeOffset());
      bean.setNextStopDistanceFromVehicle(blockLocation.getNextStop().getDistanceAlongBlock()
          - blockLocation.getDistanceAlongBlock());
    }
    
    BlockStopTimeEntry previousStop = blockLocation.getPreviousStop();
    if (previousStop != null) {
      StopTimeEntry stopTime = previousStop.getStopTime();
      StopBean stopBean = _stopBeanService.getStopForId(stopTime.getStop().getId(), null);
      bean.setPreviousStop(stopBean);
      bean.setPreviousStopTimeOffset(blockLocation.getPreviousStopTimeOffset());
      bean.setPreviousStopDistanceFromVehicle(blockLocation.getPreviousStop().getDistanceAlongBlock()
          - blockLocation.getDistanceAlongBlock());
    }

    EVehiclePhase phase = blockLocation.getPhase();
    if (phase != null)
      bean.setPhase(phase.toLabel());

    String status = blockLocation.getStatus();
    if (status != null)
      bean.setStatus(status);

    if (blockLocation.getVehicleType() != null)
      bean.setVehicleType(blockLocation.getVehicleType().toLabel());

    bean.setPredicted(blockLocation.isPredicted());

    AgencyAndId vid = blockLocation.getVehicleId();
    if (vid != null)
      bean.setVehicleId(ApplicationBeanLibrary.getId(vid));

    if (activeTripInstance != null) {
      List<ServiceAlertBean> situations = _serviceAlertBeanService.getServiceAlertsForVehicleJourney(
          time, activeTripInstance, blockLocation.getVehicleId());
      if (!situations.isEmpty())
        bean.setSituations(situations);
    }
    if (blockLocation.getTimepointPredictions() != null && blockLocation.getTimepointPredictions().size() > 0) {
      List<TimepointPredictionBean> timepointPredictions = new ArrayList<TimepointPredictionBean>();
      for (TimepointPredictionRecord tpr: blockLocation.getTimepointPredictions()) {
        TimepointPredictionBean tpb = new TimepointPredictionBean();
        if (tpr.isSkipped()) {
          tpb.setScheduleRealtionship(TimepointPredictionBean.ScheduleRelationship.SKIPPED.getValue());
        } else {
          tpb.setScheduleRealtionship(TimepointPredictionBean.ScheduleRelationship.SCHEDULED.getValue());
        }

        tpb.setTimepointId(tpr.getTimepointId().toString());
        tpb.setTripId(tpr.getTripId().toString());
        tpb.setStopSequence(tpr.getStopSequence());
        tpb.setTimepointPredictedArrivalTime(tpr.getTimepointPredictedArrivalTime());
        tpb.setTimepointPredictedDepartureTime(tpr.getTimepointPredictedDepartureTime());
        timepointPredictions.add(tpb);
      }
      bean.setTimepointPredictions(timepointPredictions);
    }
    if (blockLocation.getVehicleId() != null && blockLocation.getActiveTrip() != null) {
      VehicleOccupancyRecord vor = _vehicleOccupancyRecordCache.getRecordForVehicleIdAndRoute(blockLocation.getVehicleId(),
              blockLocation.getActiveTrip().getTrip().getRoute().getId().toString(),
              blockLocation.getActiveTrip().getTrip().getDirectionId());
      if (vor != null)
        bean.setOccupancyStatus(vor.getOccupancyStatus());
    }

    return bean;
  }

  /****
   * Private Methods
   ****/

  private ListBean<TripDetailsBean> getBlockLocationsAsTripDetails(
      List<BlockLocation> locations, TripDetailsInclusionBean inclusion,
      long time) {
    List<TripDetailsBean> tripDetails = new ArrayList<TripDetailsBean>();
    for (BlockLocation location : locations) {
      TripDetailsBean details = getBlockLocationAsTripDetails(
          location.getActiveTripInstance(), location, inclusion, time);
      tripDetails.add(details);
    }
    return new ListBean<TripDetailsBean>(tripDetails, false);
  }

  private TripDetailsBean getBlockLocationAsTripDetails(
      BlockTripInstance targetBlockTrip, BlockLocation blockLocation,
      TripDetailsInclusionBean inclusion, long time) {
    if (targetBlockTrip == null || blockLocation == null)
      return null;

    return getTripEntryAndBlockLocationAsTripDetails(targetBlockTrip,
        blockLocation, inclusion, time);
  }

  private TripDetailsBean getTripEntryAndBlockLocationAsTripDetails(
      BlockTripInstance blockTripInstance, BlockLocation blockLocation,
      TripDetailsInclusionBean inclusion, long time) {


    TripBean trip = null;
    long serviceDate = blockTripInstance.getServiceDate();
    FrequencyBean frequency = null;
    TripStopTimesBean stopTimes = null;
    TripStatusBean status = null;
    AgencyAndId vehicleId = null;

    boolean missing = false;

    FrequencyEntry frequencyLabel = blockTripInstance.getFrequencyLabel();
    if (frequencyLabel != null) {
      frequency = FrequencyBeanLibrary.getBeanForFrequency(serviceDate,
          frequencyLabel);
    }

    BlockTripEntry blockTrip = blockTripInstance.getBlockTrip();
    TripEntry tripEntry = blockTrip.getTrip();

    if (inclusion.isIncludeTripBean()) {
      trip = _tripBeanService.getTripForId(tripEntry.getId());
      if (trip == null)
        missing = true;
    }

    if (inclusion.isIncludeTripSchedule()) {
      stopTimes = _tripStopTimesBeanService.getStopTimesForBlockTrip(blockTripInstance);

      if (stopTimes == null)
        missing = true;
    }
    if (inclusion.isIncludeTripStatus() && blockLocation != null) {
      status = getBlockLocationAsStatusBean(blockLocation, time);
      vehicleId = AgencyAndIdLibrary.convertFromString(status.getVehicleId());
    }

    List<ServiceAlertBean> situations = _serviceAlertBeanService.getServiceAlertsForVehicleJourney(
        time, blockTripInstance, vehicleId);

    if (missing)
      return null;

    String tripId = AgencyAndIdLibrary.convertToString(tripEntry.getId());

    TripDetailsBean bean = new TripDetailsBean();
    bean.setTripId(tripId);
    bean.setServiceDate(serviceDate);
    bean.setFrequency(frequency);
    bean.setTrip(trip);
    bean.setSchedule(stopTimes);
    bean.setStatus(status);
    bean.setSituations(situations);
    return bean;
  }
}
