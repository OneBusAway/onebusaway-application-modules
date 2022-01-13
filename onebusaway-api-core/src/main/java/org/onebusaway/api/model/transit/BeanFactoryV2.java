/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2015 University of South Florida (cagricetin@mail.usf.edu)
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
package org.onebusaway.api.model.transit;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.onebusaway.api.impl.MaxCountSupport;
import org.onebusaway.api.model.transit.blocks.BlockConfigurationV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockInstanceV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockStopTimeV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockTripV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockV2Bean;
import org.onebusaway.api.model.transit.realtime.CurrentVehicleEstimateV2Bean;
import org.onebusaway.api.model.transit.schedule.StopTimeV2Bean;
import org.onebusaway.api.model.transit.service_alerts.*;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.realtime.api.OccupancyStatus;
import org.onebusaway.transit_data.HistoricalRidershipBean;
import org.onebusaway.transit_data.OccupancyStatusBean;
import org.onebusaway.transit_data.model.*;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.model.blocks.BlockConfigurationBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockStopTimeBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.model.config.BundleMetadata;
import org.onebusaway.transit_data.model.realtime.CurrentVehicleEstimateBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;
import org.onebusaway.transit_data.model.schedule.FrequencyBean;
import org.onebusaway.transit_data.model.schedule.FrequencyInstanceBean;
import org.onebusaway.transit_data.model.schedule.StopTimeBean;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.transit_data.model.service_alerts.TimeRangeBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.util.AgencyAndIdLibrary;

public class BeanFactoryV2 {

  private boolean _includeReferences = true;

  private boolean _includeConditionDetails = true;

  private ReferencesBean _references = new ReferencesBean();

  private MaxCountSupport _maxCount;

  private String _applicationKey;

  private Locale _locale;

  public BeanFactoryV2(boolean includeReferences) {
    _includeReferences = includeReferences;
    _locale = Locale.getDefault();
  }

  public void setIncludeConditionDetails(boolean includeConditionDetails) {
    _includeConditionDetails = includeConditionDetails;
  }

  public void setMaxCount(MaxCountSupport maxCount) {
    _maxCount = maxCount;
  }

  public void setApplicationKey(String applicationKey) {
    _applicationKey = applicationKey;
  }

  public void setLocale(Locale locale) {
    _locale = locale;
  }

  /****
   * Response Methods
   ****/

  public EntryWithReferencesBean<AgencyV2Bean> getResponse(AgencyBean agency) {
    return entry(getAgency(agency));
  }

  public EntryWithReferencesBean<RouteV2Bean> getResponse(RouteBean route) {
    return entry(getRoute(route));
  }

  public EntryWithReferencesBean<EncodedPolylineBean> getResponse(
      EncodedPolylineBean bean) {
    return entry(bean);
  }

  public Object getResponse(StopBean stop) {
    return entry(getStop(stop));
  }

  public EntryWithReferencesBean<TripV2Bean> getResponse(TripBean trip) {
    return entry(getTrip(trip));
  }

  public EntryWithReferencesBean<TripDetailsV2Bean> getResponse(
      TripDetailsBean tripDetails) {
    return entry(getTripDetails(tripDetails));
  }

  public EntryWithReferencesBean<BlockV2Bean> getBlockResponse(BlockBean block) {
    return entry(getBlock(block));
  }

  public EntryWithReferencesBean<StopWithArrivalsAndDeparturesV2Bean> getResponse(
      StopWithArrivalsAndDeparturesBean result) {
    return entry(getStopWithArrivalAndDepartures(result));
  }

  public EntryWithReferencesBean<ArrivalAndDepartureV2Bean> getResponse(
      ArrivalAndDepartureBean result) {
    return entry(getArrivalAndDeparture(result));
  }

  public EntryWithReferencesBean<StopScheduleV2Bean> getResponse(
          StopScheduleBean stopSchedule) {
    return entry(getStopSchedule(stopSchedule));
  }

  public EntryWithReferencesBean<RouteScheduleV2Bean> getResponse(
      RouteScheduleBean routeSchedule) {
    return entry(getRouteSchedule(routeSchedule)
    );
  }

  public EntryWithReferencesBean<StopsForRouteV2Bean> getResponse(
      StopsForRouteBean result, boolean includePolylines) {
    return entry(getStopsForRoute(result, includePolylines));
  }

  public EntryWithReferencesBean<ConfigV2Bean> getResponse(
      BundleMetadata result) {
    return entry(getConfig(result));
  }

  public ListWithReferencesBean<AgencyWithCoverageV2Bean> getResponse(
      List<AgencyWithCoverageBean> beans) {
    List<AgencyWithCoverageV2Bean> list = new ArrayList<AgencyWithCoverageV2Bean>();
    for (AgencyWithCoverageBean bean : filter(beans))
      list.add(getAgencyWithCoverage(bean));
    return list(list, list.size() < beans.size());
  }

  public ListWithReferencesBean<RouteV2Bean> getResponse(RoutesBean result) {
    List<RouteV2Bean> beans = new ArrayList<RouteV2Bean>();
    for (RouteBean route : result.getRoutes())
      beans.add(getRoute(route));
    return list(beans, result.isLimitExceeded(), false);
  }

  public ListWithReferencesBean<StopV2Bean> getResponse(StopsBean result) {
    List<StopV2Bean> beans = new ArrayList<StopV2Bean>();
    for (StopBean stop : result.getStops())
      beans.add(getStop(stop));
    return list(beans, result.isLimitExceeded(), false);
  }

  public ListWithReferencesBean<TripDetailsV2Bean> getTripDetailsResponse(
      ListBean<TripDetailsBean> trips) {

    List<TripDetailsV2Bean> beans = new ArrayList<TripDetailsV2Bean>();
    for (TripDetailsBean trip : trips.getList()){
      if(trip != null)
        beans.add(getTripDetails(trip));
    }

    return list(beans, trips.isLimitExceeded(), false);
  }

  public ListWithReferencesBean<VehicleStatusV2Bean> getVehicleStatusResponse(
      ListBean<VehicleStatusBean> vehicles) {

    List<VehicleStatusV2Bean> beans = new ArrayList<VehicleStatusV2Bean>();
    for (VehicleStatusBean vehicle : vehicles.getList())
      beans.add(getVehicleStatus(vehicle));
    return list(beans, vehicles.isLimitExceeded(), false);
  }

  public ListWithReferencesBean<VehicleLocationRecordV2Bean> getVehicleLocationRecordResponse(
      ListBean<VehicleLocationRecordBean> vehicles) {

    List<VehicleLocationRecordV2Bean> beans = new ArrayList<VehicleLocationRecordV2Bean>();
    for (VehicleLocationRecordBean vehicle : vehicles.getList())
      beans.add(getVehicleLocationRecord(vehicle));
    return list(beans, vehicles.isLimitExceeded(), false);
  }

  public EntryWithReferencesBean<VehicleStatusV2Bean> getVehicleStatusResponse(
      VehicleStatusBean vehicleStatus) {
    return entry(getVehicleStatus(vehicleStatus));
  }

  public ListWithReferencesBean<HistoricalRidershipBean> getHistoricalOccupancyResponse(
      List<OccupancyStatusBean> beans) {
    List<HistoricalRidershipBean> rid = new ArrayList<>();
    for (OccupancyStatusBean bean : beans) {
      rid.add(new HistoricalRidershipBean(bean.getOccupancyStatus()));
    }
    return list(rid, false, false);
  }

  public EntryWithReferencesBean<SituationV2Bean> getResponse(
      ServiceAlertBean situation) {
    return entry(getSituation(situation));
  }

  /****
   *
   *****/

  public ListWithReferencesBean<String> getEntityIdsResponse(
      ListBean<String> ids) {
    return list(ids.getList(), ids.isLimitExceeded());
  }

  public <T> ListWithReferencesBean<T> getEmptyList(Class<T> type,
      boolean outOfRange) {
    return list(new ArrayList<T>(), false, outOfRange);
  }

  /****
   *
   ***/

  public TimeIntervalV2 getTimeInterval(TimeIntervalBean interval) {
    if (interval == null)
      return null;
    TimeIntervalV2 bean = new TimeIntervalV2();
    bean.setFrom(interval.getFrom());
    bean.setTo(interval.getTo());
    return bean;
  }

  public AgencyV2Bean getAgency(AgencyBean agency) {
    AgencyV2Bean bean = new AgencyV2Bean();
    bean.setDisclaimer(agency.getDisclaimer());
    bean.setId(agency.getId());
    bean.setLang(agency.getLang());
    bean.setName(agency.getName());
    bean.setPhone(agency.getPhone());
    bean.setPrivateService(agency.isPrivateService());
    bean.setTimezone(agency.getTimezone());
    bean.setUrl(agency.getUrl());
    bean.setEmail(agency.getEmail());
    bean.setFareUrl(agency.getFareUrl());
    return bean;
  }

  public RouteV2Bean getRoute(RouteBean route) {
    RouteV2Bean bean = new RouteV2Bean();

    bean.setAgencyId(route.getAgency().getId());
    addToReferences(route.getAgency());

    bean.setColor(route.getColor());
    bean.setDescription(route.getDescription());
    bean.setId(route.getId());
    bean.setLongName(route.getLongName());
    bean.setShortName(route.getShortName());
    bean.setTextColor(route.getTextColor());
    bean.setType(route.getType());
    bean.setUrl(route.getUrl());

    return bean;
  }

  public ConfigV2Bean getConfig(BundleMetadata meta) {
    ConfigV2Bean bean = new ConfigV2Bean();
    bean.setGitProperties(getGitProperties());
    if (meta == null) return bean;
    bean.setId(meta.getId());
    bean.setName(meta.getName());
    bean.setServiceDateFrom(meta.getServiceDateFrom());
    bean.setServiceDateTo(meta.getServiceDateTo());
    return bean;
  }

  public StopV2Bean getStop(StopBean stop) {
    StopV2Bean bean = new StopV2Bean();
    bean.setCode(stop.getCode());
    bean.setDirection(stop.getDirection());
    bean.setId(stop.getId());
    bean.setLat(stop.getLat());
    bean.setLon(stop.getLon());
    bean.setLocationType(stop.getLocationType());
    bean.setName(stop.getName());
    bean.setWheelchairBoarding(stop.getWheelchairBoarding());

    List<String> routeIds = new ArrayList<String>();
    for (RouteBean route : stop.getRoutes()) {
      routeIds.add(route.getId());
      addToReferences(route);
    }
    bean.setRouteIds(routeIds);

    return bean;
  }

  public TripV2Bean getTrip(TripBean trip) {

    TripV2Bean bean = new TripV2Bean();

    bean.setId(trip.getId());

    bean.setRouteId(trip.getRoute().getId());
    addToReferences(trip.getRoute());

    bean.setRouteShortName(trip.getRouteShortName());
    bean.setTripHeadsign(trip.getTripHeadsign());
    bean.setTripShortName(trip.getTripShortName());

    bean.setDirectionId(trip.getDirectionId());
    bean.setServiceId(trip.getServiceId());
    bean.setShapeId(trip.getShapeId());
    bean.setBlockId(trip.getBlockId());

    return bean;
  }

  public TripStatusV2Bean getTripStatus(TripStatusBean tripStatus) {

    TripStatusV2Bean bean = new TripStatusV2Bean();

    TripBean activeTrip = tripStatus.getActiveTrip();
    if (activeTrip != null) {
      bean.setActiveTripId(activeTrip.getId());
      bean.setBlockTripSequence(tripStatus.getBlockTripSequence());
      addToReferences(activeTrip);
    }

    bean.setServiceDate(tripStatus.getServiceDate());

    FrequencyBean frequency = tripStatus.getFrequency();
    if (frequency != null)
      bean.setFrequency(getFrequency(frequency));

    bean.setScheduledDistanceAlongTrip(tripStatus.getScheduledDistanceAlongTrip());
    bean.setTotalDistanceAlongTrip(tripStatus.getTotalDistanceAlongTrip());

    bean.setPosition(tripStatus.getLocation());
    if (tripStatus.isOrientationSet())
      bean.setOrientation(tripStatus.getOrientation());

    StopBean closestStop = tripStatus.getClosestStop();
    if (closestStop != null) {
      bean.setClosestStop(closestStop.getId());
      addToReferences(closestStop);
      bean.setClosestStopTimeOffset(tripStatus.getClosestStopTimeOffset());
    }

    StopBean nextStop = tripStatus.getNextStop();
    if (nextStop != null) {
      bean.setNextStop(nextStop.getId());
      addToReferences(nextStop);
      bean.setNextStopTimeOffset(tripStatus.getNextStopTimeOffset());
    }

    bean.setPhase(tripStatus.getPhase());
    bean.setStatus(tripStatus.getStatus());

    bean.setPredicted(tripStatus.isPredicted());

    if (tripStatus.getLastUpdateTime() > 0)
      bean.setLastUpdateTime(tripStatus.getLastUpdateTime());

    if (tripStatus.getLastLocationUpdateTime() > 0)
      bean.setLastLocationUpdateTime(tripStatus.getLastLocationUpdateTime());

    if (tripStatus.isLastKnownDistanceAlongTripSet())
      bean.setLastKnownDistanceAlongTrip(tripStatus.getLastKnownDistanceAlongTrip());

    bean.setLastKnownLocation(tripStatus.getLastKnownLocation());

    if (tripStatus.isLastKnownOrientationSet())
      bean.setLastKnownOrientation(tripStatus.getLastKnownOrientation());

    if (tripStatus.isScheduleDeviationSet())
      bean.setScheduleDeviation((int) tripStatus.getScheduleDeviation());
    if (tripStatus.isDistanceAlongTripSet())
      bean.setDistanceAlongTrip(tripStatus.getDistanceAlongTrip());
    bean.setVehicleId(tripStatus.getVehicleId());

    if (tripStatus.getOccupancyStatus() != null)
      bean.setOccupancyStatus(OccupancyStatus.valueOf(tripStatus.getOccupancyStatus()));

    List<ServiceAlertBean> situations = tripStatus.getSituations();
    if (situations != null && !situations.isEmpty()) {
      List<String> situationIds = new ArrayList<String>();
      for (ServiceAlertBean situation : situations) {
        situationIds.add(situation.getId());
        addToReferences(situation);
      }
      bean.setSituationIds(situationIds);
    }

    return bean;
  }

  public TripStopTimesV2Bean getTripStopTimes(TripStopTimesBean tripStopTimes) {

    TripStopTimesV2Bean bean = new TripStopTimesV2Bean();

    bean.setTimeZone(tripStopTimes.getTimeZone());

    List<TripStopTimeV2Bean> instances = new ArrayList<TripStopTimeV2Bean>();
    for (TripStopTimeBean sti : tripStopTimes.getStopTimes()) {

      TripStopTimeV2Bean stiBean = new TripStopTimeV2Bean();
      stiBean.setArrivalTime(sti.getArrivalTime());
      stiBean.setDepartureTime(sti.getDepartureTime());
      stiBean.setStopHeadsign(sti.getStopHeadsign());
      stiBean.setDistanceAlongTrip(sti.getDistanceAlongTrip());

      stiBean.setStopId(sti.getStop().getId());
      stiBean.setHistoricalOccupancy(sti.getHistoricalOccupancy());

      addToReferences(sti.getStop());

      instances.add(stiBean);
    }

    bean.setStopTimes(instances);

    TripBean nextTrip = tripStopTimes.getNextTrip();
    if (nextTrip != null) {
      bean.setNextTripId(nextTrip.getId());
      addToReferences(nextTrip);
    }

    TripBean prevTrip = tripStopTimes.getPreviousTrip();
    if (prevTrip != null) {
      bean.setPreviousTripId(prevTrip.getId());
      addToReferences(prevTrip);
    }

    FrequencyBean freq = tripStopTimes.getFrequency();
    if (freq != null)
      bean.setFrequency(getFrequency(freq));

    return bean;
  }

  public TripDetailsV2Bean getTripDetails(TripDetailsBean tripDetails) {

    TripDetailsV2Bean bean = new TripDetailsV2Bean();

    bean.setTripId(tripDetails.getTripId());
    bean.setServiceDate(tripDetails.getServiceDate());

    if (tripDetails.getFrequency() != null)
      bean.setFrequency(getFrequency(tripDetails.getFrequency()));

    TripBean trip = tripDetails.getTrip();
    if (trip != null)
      addToReferences(trip);

    TripStopTimesBean stopTimes = tripDetails.getSchedule();
    if (stopTimes != null)
      bean.setSchedule(getTripStopTimes(stopTimes));

    TripStatusBean status = tripDetails.getStatus();
    if (status != null)
      bean.setStatus(getTripStatus(status));

    List<ServiceAlertBean> situations = tripDetails.getSituations();
    if (!CollectionsLibrary.isEmpty(situations)) {
      List<String> situationIds = new ArrayList<String>();
      for (ServiceAlertBean situation : situations) {
        addToReferences(situation);
        situationIds.add(situation.getId());
      }
      bean.setSituationIds(situationIds);
    }

    return bean;
  }

  public BlockInstanceV2Bean getBlockInstance(BlockInstanceBean blockInstance) {
    BlockInstanceV2Bean bean = new BlockInstanceV2Bean();
    bean.setBlockConfiguration(getBlockConfig(blockInstance.getBlockConfiguration()));
    bean.setBlockId(blockInstance.getBlockId());
    bean.setServiceDate(blockInstance.getServiceDate());
    return bean;
  }

  public BlockV2Bean getBlock(BlockBean block) {
    BlockV2Bean bean = new BlockV2Bean();
    bean.setId(block.getId());
    List<BlockConfigurationV2Bean> blockConfigs = new ArrayList<BlockConfigurationV2Bean>();
    for (BlockConfigurationBean blockConfig : block.getConfigurations())
      blockConfigs.add(getBlockConfig(blockConfig));
    bean.setConfigurations(blockConfigs);
    return bean;
  }

  public BlockConfigurationV2Bean getBlockConfig(
      BlockConfigurationBean blockConfig) {
    BlockConfigurationV2Bean bean = new BlockConfigurationV2Bean();
    bean.setActiveServiceIds(blockConfig.getActiveServiceIds());
    bean.setInactiveServiceIds(blockConfig.getInactiveServiceIds());
    List<BlockTripV2Bean> blockTrips = new ArrayList<BlockTripV2Bean>();
    for (BlockTripBean blockTrip : blockConfig.getTrips())
      blockTrips.add(getBlockTrip(blockTrip));
    bean.setTrips(blockTrips);
    return bean;
  }

  public BlockTripV2Bean getBlockTrip(BlockTripBean blockTrip) {

    BlockTripV2Bean bean = new BlockTripV2Bean();
    bean.setAccumulatedSlackTime(blockTrip.getAccumulatedSlackTime());
    bean.setDistanceAlongBlock(blockTrip.getDistanceAlongBlock());

    addToReferences(blockTrip.getTrip());
    bean.setTripId(blockTrip.getTrip().getId());

    List<BlockStopTimeV2Bean> blockStopTimes = new ArrayList<BlockStopTimeV2Bean>();
    for (BlockStopTimeBean blockStopTime : blockTrip.getBlockStopTimes()) {
      BlockStopTimeV2Bean stopTimeBean = getBlockStopTime(blockStopTime);
      blockStopTimes.add(stopTimeBean);
    }
    bean.setBlockStopTimes(blockStopTimes);

    return bean;
  }

  public BlockStopTimeV2Bean getBlockStopTime(BlockStopTimeBean blockStopTime) {
    BlockStopTimeV2Bean bean = new BlockStopTimeV2Bean();
    bean.setAccumulatedSlackTime(blockStopTime.getAccumulatedSlackTime());
    bean.setBlockSequence(blockStopTime.getBlockSequence());
    bean.setDistanceAlongBlock(blockStopTime.getDistanceAlongBlock());
    bean.setStopTime(getStopTime(blockStopTime.getStopTime()));
    return bean;
  }

  public StopTimeV2Bean getStopTime(StopTimeBean stopTime) {
    StopTimeV2Bean bean = new StopTimeV2Bean();
    bean.setArrivalTime(stopTime.getArrivalTime());
    bean.setDepartureTime(stopTime.getDepartureTime());
    bean.setDropOffType(stopTime.getDropOffType());
    bean.setPickupType(stopTime.getPickupType());

    bean.setStopId(stopTime.getStop().getId());
    addToReferences(stopTime.getStop());

    return bean;
  }

  public ScheduleStopTimeInstanceExtendedWithStopIdV2Bean getStopTime(StopTimeInstanceBeanExtendedWithStopId stopTime) {
    ScheduleStopTimeInstanceExtendedWithStopIdV2Bean bean = new ScheduleStopTimeInstanceExtendedWithStopIdV2Bean();
    bean.setArrivalTime(stopTime.getArrivalTime());
    bean.setDepartureTime(stopTime.getDepartureTime());
    bean.setArrivalEnabled(stopTime.isArrivalEnabled());
    bean.setDepartureEnabled(stopTime.isDepartureEnabled());
    bean.setStopHeadsign(stopTime.getStopHeadsign());
    bean.setTripId(stopTime.getTripId());
    bean.setStopId(stopTime.getStopId().toString());

    return bean;
  }

  public ListWithReferencesBean<CurrentVehicleEstimateV2Bean> getCurrentVehicleEstimates(
      ListBean<CurrentVehicleEstimateBean> estimates) {

    if (estimates == null || estimates.getList() == null)
      return list(new ArrayList<CurrentVehicleEstimateV2Bean>(), false);

    List<CurrentVehicleEstimateV2Bean> beans = new ArrayList<CurrentVehicleEstimateV2Bean>();
    for (CurrentVehicleEstimateBean estimate : estimates.getList())
      beans.add(getCurrentVehicleEstimate(estimate));

    return list(beans, estimates.isLimitExceeded());
  }

  public CurrentVehicleEstimateV2Bean getCurrentVehicleEstimate(
      CurrentVehicleEstimateBean estimate) {

    if (estimate == null)
      return null;

    CurrentVehicleEstimateV2Bean bean = new CurrentVehicleEstimateV2Bean();
    bean.setProbability(estimate.getProbability());
    bean.setTripStatus(getTripStatus(estimate.getTripStatus()));
    bean.setDebug(estimate.getDebug());
    return bean;
  }

  public VehicleStatusV2Bean getVehicleStatus(VehicleStatusBean vehicleStatus) {

    VehicleStatusV2Bean bean = new VehicleStatusV2Bean();

    bean.setLastUpdateTime(vehicleStatus.getLastUpdateTime());
    if (vehicleStatus.getLastLocationUpdateTime() > 0)
      bean.setLastLocationUpdateTime(vehicleStatus.getLastLocationUpdateTime());
    bean.setLocation(vehicleStatus.getLocation());
    bean.setPhase(vehicleStatus.getPhase());
    bean.setStatus(vehicleStatus.getStatus());
    bean.setVehicleId(vehicleStatus.getVehicleId());
    if (vehicleStatus.getOccupancyStatus() != null &&
            vehicleStatus.getOccupancyStatus() != OccupancyStatus.UNKNOWN) {
      bean.setOccupancyStatus(vehicleStatus.getOccupancyStatus());
    } else {
      bean.setOccupancyStatus(null);
    }

    if(vehicleStatus.getOccupancyCount() != null){
      bean.setOccupancyCount(vehicleStatus.getOccupancyCount());
    }

    if(vehicleStatus.getOccupancyCapacity() != null){
      bean.setOccupancyCapacity(vehicleStatus.getOccupancyCapacity());
    }

    TripBean trip = vehicleStatus.getTrip();
    if (trip != null) {
      bean.setTripId(trip.getId());
      addToReferences(trip);
    }

    TripStatusBean tripStatus = vehicleStatus.getTripStatus();
    if (tripStatus != null)
      bean.setTripStatus(getTripStatus(tripStatus));

    return bean;
  }

  public VehicleLocationRecordV2Bean getVehicleLocationRecord(
      VehicleLocationRecordBean record) {

    VehicleLocationRecordV2Bean bean = new VehicleLocationRecordV2Bean();

    bean.setBlockId(record.getBlockId());
    bean.setCurrentLocation(record.getCurrentLocation());
    if (record.isCurrentOrientationSet())
      bean.setCurrentOrientation(record.getCurrentOrientation());
    if (record.isDistanceAlongBlockSet())
      bean.setDistanceAlongBlock(record.getDistanceAlongBlock());
    bean.setPhase(record.getPhase());
    if (record.isScheduleDeviationSet())
      bean.setScheduleDeviation(record.getScheduleDeviation());
    bean.setServiceDate(record.getServiceDate());
    bean.setStatus(record.getStatus());
    bean.setTimeOfRecord(record.getTimeOfRecord());
    bean.setTimeOfLocationUpdate(record.getTimeOfLocationUpdate());
    bean.setTripId(record.getTripId());
    bean.setVehicleId(record.getVehicleId());
    return bean;
  }

  /**
   * Support Schedule queries at the route level.  Similar to
   * StopScheduleBeanServiceImpl.
   *
   *   Ultimate goal to deliver data in this format:
   *     "entry": {
   *   "routeId": "40_100479",
   *   "serviceIds": ["SERVICEIDVALUE1","SERVICEIDVALUE2"],
   *   "scheduleDate": 1609315200,
   *   "stopTripGroupings": [
   *     {
   *       "directionId": 0,
   *       "tripHeadsign": "University of Washington Station",
   *       "stopIds": ["STOPID1", "STOPID2"],
   *       "tripIds": ["TRIPID1", "TRIPID2"]
   *     },
   *     {
   *       "directionId": 1,
   *       "tripHeadsign": "Angle Lake Station",
   *       "stopIds": ["STOPID2", "STOPID3"],
   *       "tripIds": ["TRIPID3", "TRIPID4"]
   *     }
   *   ]
   * }
   */
  public RouteScheduleV2Bean getRouteSchedule(RouteScheduleBean routeSchedule) {

    RouteScheduleV2Bean bean = new RouteScheduleV2Bean();

    bean.setRouteId(AgencyAndIdLibrary.convertToString(routeSchedule.getRouteId()));

    bean.setServiceIds(routeSchedule.getServiceIds().stream().map(
            serviceId -> serviceId.toString()).collect(Collectors.toList()));

    bean.setScheduleDate(routeSchedule.getScheduleDate().getAsDate().getTime());


    Comparator<StopTimeInstanceBean> comparator = new Comparator<StopTimeInstanceBean>() {
      @Override
      public int compare(StopTimeInstanceBean s1, StopTimeInstanceBean s2) {
        if(!s1.getTripId().equals(s2.getTripId())){
          return s1.getTripId().compareTo(s2.getTripId());
        }
        return s1.getArrivalTime()<s2.getArrivalTime() ? -1 :1;
      }
    };

    List<StopsAndTripsForDirectionV2Bean> stopTripDirectionBeans = new ArrayList<>();
    for (StopsAndTripsForDirectionBean stdb : routeSchedule.getStopTripDirections()) {
      StopsAndTripsForDirectionV2Bean v2 = new StopsAndTripsForDirectionV2Bean();
      v2.setDirectionId(stdb.getDirectionId());
      v2.setTripHeadsigns(stdb.getTripHeadsigns());
      v2.setStopIds(stdb.getStopIds().stream().map(x->x.toString()).collect(Collectors.toList()));
      Collections.sort(stdb.getTripIds());
      v2.setTripIds(stdb.getTripIds().stream().map(x->x.toString()).collect(Collectors.toList()));
      Collections.sort(stdb.getStopTimes(),comparator);
      List<? extends StopTimeInstanceBean> stopTimesBeans =  stdb.getStopTimes();
      v2.setTripsWithStopTimes(stdb.getTripIds().stream().
              map(x-> getStopTimesForTrip(x.toString(), (List<StopTimeInstanceBeanExtendedWithStopId>) stopTimesBeans))
              .collect(Collectors.toList()));
      stopTripDirectionBeans.add(v2);
    }
    bean.setStopTripGroupings(stopTripDirectionBeans);

    _references.setAgencies(routeSchedule.getAgencies().stream()
            .map(x->{ return getAgency(x);})
            .collect(Collectors.toList()));
    _references.setRoutes(routeSchedule.getRoutes().stream()
            .map(x->{ return getRoute(x);})
            .collect(Collectors.toList()));
    _references.setSituations(routeSchedule.getServiceAlerts().stream()
            .map(x->{ return getSituation(x);})
            .collect(Collectors.toList()));
    _references.setStops(routeSchedule.getStops().stream()
            .map(x->{ return getStop(x);})
            .collect(Collectors.toList()));
    _references.setTrips(routeSchedule.getTrips().stream()
            .map(x->{ return getTrip(x);})
            .collect(Collectors.toList()));
    _references.setStopTimes(routeSchedule.getStopTimes().stream()
            .map(x->{ return getStopTime(x);})
            .collect(Collectors.toList()));

    return bean;
  }

  private TripWithStopTimesV2Bean getStopTimesForTrip(
          String tripId,
          List<StopTimeInstanceBeanExtendedWithStopId> sortedStoptimesList){
    int index = getIndexOfFirstStopTimeMatchForTrip(sortedStoptimesList,tripId);
    List stopTimesForTrip = new ArrayList<ScheduleStopTimeInstanceV2Bean>();
    StopTimeInstanceBeanExtendedWithStopId stopTime = sortedStoptimesList.get(index);
    while (stopTime.getTripId().equals(tripId)){
      ScheduleStopTimeInstanceExtendedWithStopIdV2Bean v2 = getStopTime(stopTime);
      stopTimesForTrip.add(v2);
      index++;
      if(index>=sortedStoptimesList.size()){
        break;
      }
      stopTime = sortedStoptimesList.get(index);
    }
    TripWithStopTimesV2Bean tripWithStopTimes = new TripWithStopTimesV2Bean();
    tripWithStopTimes.setTripId(tripId);
    tripWithStopTimes.setStopTimes(stopTimesForTrip);
    return tripWithStopTimes;
  }

  private int getIndexOfFirstStopTimeMatchForTrip(List<StopTimeInstanceBeanExtendedWithStopId> sortedStoptimesList, String trip){
    int i = getIndexStopTimesByTrip(sortedStoptimesList, trip);
    if(i==0){return i;}
    while(true){
      i--;
      if(!sortedStoptimesList.get(i).getTripId().equals(trip)){
        return i+1;
      }
    }

  }


  private int getIndexStopTimesByTrip(List<StopTimeInstanceBeanExtendedWithStopId> sortedStoptimesList, String trip){
    int min = 0;
    int max = sortedStoptimesList.size() -1;
    int i = 0;
    if (max ==min){return -1;}
    if (max == i) {return 0;}
    while(true){
      int comparison = sortedStoptimesList.get(i).getTripId().compareTo(trip);
      if(comparison<0) {
        min = i;
        i = (min + max) / 2 + 1;
      }
      else if (comparison>0){
        max = i;
        i = (min + max)/2;
      }
      else{
        return i;
      }
    }
  }

  public StopScheduleV2Bean getStopSchedule(StopScheduleBean stopSchedule) {

    StopScheduleV2Bean bean = new StopScheduleV2Bean();

    StopBean stop = stopSchedule.getStop();
    if (stop != null) {
      addToReferences(stop);
      bean.setStopId(stop.getId());
    }

    bean.setDate(stopSchedule.getDate().getTime());

    List<StopRouteScheduleV2Bean> stopRouteScheduleBeans = new ArrayList<StopRouteScheduleV2Bean>();

    for (StopRouteScheduleBean stopRouteSchedule : stopSchedule.getRoutes()) {
      StopRouteScheduleV2Bean stopRouteScheduleBean = getStopRouteSchedule(stopRouteSchedule);
      stopRouteScheduleBeans.add(stopRouteScheduleBean);
    }
    bean.setStopRouteSchedules(stopRouteScheduleBeans);

    /*
     * StopCalendarDaysBean days = stopSchedule.getCalendarDays();
     * bean.setTimeZone(days.getTimeZone());
     *
     * List<StopCalendarDayV2Bean> dayBeans = new
     * ArrayList<StopCalendarDayV2Bean>(); for (StopCalendarDayBean day :
     * days.getDays()) { StopCalendarDayV2Bean dayBean =
     * getStopCalendarDay(day); dayBeans.add(dayBean); }
     * bean.setStopCalendarDays(dayBeans);
     */

    return bean;
  }

  public StopRouteScheduleV2Bean getStopRouteSchedule(
      StopRouteScheduleBean stopRouteSchedule) {

    StopRouteScheduleV2Bean bean = new StopRouteScheduleV2Bean();

    bean.setRouteId(stopRouteSchedule.getRoute().getId());
    addToReferences(stopRouteSchedule.getRoute());

    List<StopRouteDirectionScheduleV2Bean> directions = bean.getStopRouteDirectionSchedules();
    for (StopRouteDirectionScheduleBean direction : stopRouteSchedule.getDirections())
      directions.add(getStopRouteDirectionSchedule(direction));

    return bean;
  }

  public StopRouteDirectionScheduleV2Bean getStopRouteDirectionSchedule(
      StopRouteDirectionScheduleBean direction) {

    StopRouteDirectionScheduleV2Bean bean = new StopRouteDirectionScheduleV2Bean();
    bean.setTripHeadsign(direction.getTripHeadsign());

    List<ScheduleStopTimeInstanceV2Bean> stopTimes = new ArrayList<ScheduleStopTimeInstanceV2Bean>();
    for (StopTimeInstanceBean sti : direction.getStopTimes()) {
      ScheduleStopTimeInstanceV2Bean stiBean = new ScheduleStopTimeInstanceV2Bean();
      stiBean.setArrivalEnabled(sti.isArrivalEnabled());
      stiBean.setArrivalTime(sti.getArrivalTime());
      stiBean.setDepartureEnabled(sti.isDepartureEnabled());
      stiBean.setDepartureTime(sti.getDepartureTime());
      stiBean.setServiceId(sti.getServiceId());
      stiBean.setTripId(sti.getTripId());
      stiBean.setStopHeadsign(stiBean.getStopHeadsign());
      stopTimes.add(stiBean);
    }

    if (!stopTimes.isEmpty())
      bean.setScheduleStopTimes(stopTimes);

    List<ScheduleFrequencyInstanceV2Bean> frequencies = new ArrayList<ScheduleFrequencyInstanceV2Bean>();
    for (FrequencyInstanceBean freq : direction.getFrequencies()) {
      ScheduleFrequencyInstanceV2Bean freqBean = new ScheduleFrequencyInstanceV2Bean();
      freqBean.setServiceDate(freq.getServiceDate());
      freqBean.setServiceId(freq.getServiceId());
      freqBean.setTripId(freq.getTripId());
      freqBean.setStartTime(freq.getStartTime());
      freqBean.setEndTime(freq.getEndTime());
      freqBean.setHeadway(freq.getHeadwaySecs());
      freqBean.setStopHeadsign(freq.getStopHeadsign());
      freqBean.setArrivalEnabled(freq.isArrivalEnabled());
      freqBean.setDepartureEnabled(freq.isDepartureEnabled());
      frequencies.add(freqBean);
    }

    if (!frequencies.isEmpty())
      bean.setScheduleFrequencies(frequencies);

    return bean;
  }

  public StopCalendarDayV2Bean getStopCalendarDay(StopCalendarDayBean day) {
    StopCalendarDayV2Bean bean = new StopCalendarDayV2Bean();
    bean.setDate(day.getDate().getTime());
    bean.setGroup(day.getGroup());
    return bean;
  }

  public StopsForRouteV2Bean getStopsForRoute(StopsForRouteBean stopsForRoute,
      boolean includePolylines) {
    StopsForRouteV2Bean bean = new StopsForRouteV2Bean();

    RouteBean route = stopsForRoute.getRoute();
    if (route != null) {
      addToReferences(route);
      bean.setRouteId(route.getId());
    }

    List<String> stopIds = new ArrayList<String>();
    for (StopBean stop : stopsForRoute.getStops()) {
      stopIds.add(stop.getId());
      addToReferences(stop);
    }
    bean.setStopIds(stopIds);
    bean.setStopGroupings(stopsForRoute.getStopGroupings());
    if (!includePolylines) {
      for (StopGroupingBean grouping : stopsForRoute.getStopGroupings()) {
        for (StopGroupBean group : grouping.getStopGroups())
          group.setPolylines(null);
      }
    }
    if (includePolylines)
      bean.setPolylines(stopsForRoute.getPolylines());
    return bean;
  }

  public StopWithArrivalsAndDeparturesV2Bean getStopWithArrivalAndDepartures(
      StopWithArrivalsAndDeparturesBean sad) {
    StopWithArrivalsAndDeparturesV2Bean bean = new StopWithArrivalsAndDeparturesV2Bean();

    bean.setStopId(sad.getStop().getId());
    addToReferences(sad.getStop());

    List<ArrivalAndDepartureV2Bean> ads = new ArrayList<ArrivalAndDepartureV2Bean>();
    for (ArrivalAndDepartureBean ad : sad.getArrivalsAndDepartures())
      ads.add(getArrivalAndDeparture(ad));
    bean.setArrivalsAndDepartures(ads);

    List<String> nearbyStopIds = new ArrayList<String>();
    for (StopBean nearbyStop : sad.getNearbyStops()) {
      nearbyStopIds.add(nearbyStop.getId());
      addToReferences(nearbyStop);
    }
    bean.setNearbyStopIds(nearbyStopIds);

    List<ServiceAlertBean> situations = sad.getSituations();
    if (!CollectionsLibrary.isEmpty(situations)) {
      List<String> situationIds = new ArrayList<String>();
      for (ServiceAlertBean situation : situations) {
        addToReferences(situation);
        situationIds.add(situation.getId());
      }
      bean.setSituationIds(situationIds);
    }

    return bean;
  }

  public ArrivalAndDepartureV2Bean getArrivalAndDeparture(
      ArrivalAndDepartureBean ad) {

    TripBean trip = ad.getTrip();
    RouteBean route = trip.getRoute();
    StopBean stop = ad.getStop();

    ArrivalAndDepartureV2Bean bean = new ArrivalAndDepartureV2Bean();

    bean.setTripId(trip.getId());
    addToReferences(trip);

    bean.setServiceDate(ad.getServiceDate());
    bean.setVehicleId(ad.getVehicleId());
    bean.setStopId(stop.getId());
    addToReferences(stop);
    bean.setStopSequence(ad.getStopSequence());
    bean.setBlockTripSequence(ad.getBlockTripSequence());
    bean.setTotalStopsInTrip(ad.getTotalStopsInTrip());
    
    bean.setRouteId(route.getId());
    addToReferences(route);

    String routeShortName = ad.getRouteShortName();
    if (routeShortName == null || routeShortName.isEmpty())
      routeShortName = trip.getRouteShortName();
    if (routeShortName == null || routeShortName.isEmpty())
      routeShortName = route.getShortName();
    bean.setRouteShortName(routeShortName);

    bean.setRouteLongName(route.getLongName());

    String tripHeadsign = ad.getTripHeadsign();
    if (tripHeadsign == null || tripHeadsign.isEmpty())
      tripHeadsign = trip.getTripHeadsign();
    bean.setTripHeadsign(tripHeadsign);

    bean.setArrivalEnabled(ad.isArrivalEnabled());
    bean.setDepartureEnabled(ad.isDepartureEnabled());

    bean.setScheduledArrivalTime(ad.getScheduledArrivalTime());
    bean.setScheduledDepartureTime(ad.getScheduledDepartureTime());
    bean.setPredictedArrivalTime(ad.getPredictedArrivalTime());
    bean.setPredictedDepartureTime(ad.getPredictedDepartureTime());
    bean.setHistoricalOccupancy(ad.getHistoricalOccupancy());
    bean.setOccupancyStatus(ad.getOccupancyStatus());

    bean.setScheduledArrivalInterval(getTimeInterval(ad.getScheduledArrivalInterval()));
    bean.setScheduledDepartureInterval(getTimeInterval(ad.getScheduledDepartureInterval()));
    bean.setPredictedArrivalInterval(getTimeInterval(ad.getPredictedArrivalInterval()));
    bean.setPredictedDepartureInterval(getTimeInterval(ad.getPredictedDepartureInterval()));

    if (ad.getFrequency() != null)
      bean.setFrequency(getFrequency(ad.getFrequency()));

    bean.setStatus(ad.getStatus());

    if (ad.isDistanceFromStopSet())
      bean.setDistanceFromStop(ad.getDistanceFromStop());

    bean.setNumberOfStopsAway(ad.getNumberOfStopsAway());

    TripStatusBean tripStatus = ad.getTripStatus();
    if (tripStatus != null)
      bean.setTripStatus(getTripStatus(tripStatus));

    bean.setPredicted(ad.isPredicted());
    bean.setLastUpdateTime(ad.getLastUpdateTime());

    List<ServiceAlertBean> situations = ad.getSituations();
    if (situations != null && !situations.isEmpty()) {
      List<String> situationIds = new ArrayList<String>();
      for (ServiceAlertBean situation : situations) {
        situationIds.add(situation.getId());
        addToReferences(situation);
      }
      bean.setSituationIds(situationIds);
    }

    return bean;
  }

  public FrequencyV2Bean getFrequency(FrequencyBean frequency) {
    FrequencyV2Bean bean = new FrequencyV2Bean();
    bean.setStartTime(frequency.getStartTime());
    bean.setEndTime(frequency.getEndTime());
    bean.setHeadway(frequency.getHeadway());
    bean.setExactTimes(frequency.getExactTimes());
    return bean;
  }

  public FrequencyBean reverseFrequency(FrequencyV2Bean frequency) {
    FrequencyBean bean = new FrequencyBean();
    bean.setStartTime(frequency.getStartTime());
    bean.setEndTime(frequency.getEndTime());
    bean.setHeadway(frequency.getHeadway());
    bean.setExactTimes(frequency.getExactTimes());
    return bean;
  }

  public boolean isSituationExcludedForApplication(ServiceAlertBean situation) {
    List<SituationAffectsBean> affects = situation.getAllAffects();
    if (affects == null)
      return false;
    Set<String> applicationIds = new HashSet<String>();
    for (SituationAffectsBean affect : affects) {
      if (affect.getApplicationId() != null)
        applicationIds.add(affect.getApplicationId());
    }
    if (CollectionsLibrary.isEmpty(applicationIds))
      return false;
    if (_applicationKey == null)
      return true;
    return !_applicationKey.contains(_applicationKey);
  }

  public SituationV2Bean getSituation(ServiceAlertBean situation) {

    SituationV2Bean bean = new SituationV2Bean();

    bean.setId(situation.getId());
    bean.setCreationTime(situation.getCreationTime());

    if (!CollectionsLibrary.isEmpty(situation.getActiveWindows())) {
      List<TimeRangeV2Bean> activeWindows = new ArrayList<TimeRangeV2Bean>();
      for (TimeRangeBean activeWindow : situation.getActiveWindows())
        activeWindows.add(getTimeRange(activeWindow));
      bean.setActiveWindows(activeWindows);
    }

    if (!CollectionsLibrary.isEmpty(situation.getPublicationWindows())) {
      List<TimeRangeV2Bean> publicationWindows = new ArrayList<TimeRangeV2Bean>();
      for (TimeRangeBean publicationWindow : situation.getPublicationWindows())
        publicationWindows.add(getTimeRange(publicationWindow));
      bean.setPublicationWindows(publicationWindows);
    }

    if (!CollectionsLibrary.isEmpty(situation.getAllAffects())) {
      List<SituationAffectsV2Bean> affects = new ArrayList<SituationAffectsV2Bean>();
      for (SituationAffectsBean affect : situation.getAllAffects())
        affects.add(getSituationAffects(affect));
      bean.setAllAffects(affects);
    }

    if (!CollectionsLibrary.isEmpty(situation.getConsequences())) {
      List<SituationConsequenceV2Bean> beans = new ArrayList<SituationConsequenceV2Bean>();
      for (SituationConsequenceBean consequence : situation.getConsequences()) {
        SituationConsequenceV2Bean consequenceBean = getSituationConsequence(consequence);
        beans.add(consequenceBean);
      }
      bean.setConsequences(beans);
    }

    bean.setReason(situation.getReason());

    bean.setSummary(getBestString(situation.getSummaries()));
    bean.setDescription(getBestString(situation.getDescriptions()));
    bean.setUrl(getBestString(situation.getUrls()));

    ESeverity severity = situation.getSeverity();
    if (severity != null) {
      String[] codes = severity.getTpegCodes();
      bean.setSeverity(codes[0]);
    }

    return bean;
  }

  public SituationAffectsV2Bean getSituationAffects(SituationAffectsBean affects) {

    SituationAffectsV2Bean bean = new SituationAffectsV2Bean();
    bean.setAgencyId(affects.getAgencyId());
    bean.setApplicationId(affects.getApplicationId());
    bean.setDirectionId(affects.getDirectionId());
    bean.setRouteId(affects.getRouteId());
    bean.setStopId(affects.getStopId());
    bean.setTripId(affects.getTripId());

    return bean;
  }

  private SituationConsequenceV2Bean getSituationConsequence(
      SituationConsequenceBean consequence) {

    SituationConsequenceV2Bean bean = new SituationConsequenceV2Bean();

    if (consequence.getEffect() != null)
      bean.setCondition(consequence.getEffect().toString().toLowerCase());

    if (_includeConditionDetails
        && (consequence.getDetourPath() != null || !CollectionsLibrary.isEmpty(consequence.getDetourStopIds()))) {
      SituationConditionDetailsV2Bean detailsBean = new SituationConditionDetailsV2Bean();
      if (consequence.getDetourPath() != null) {
        EncodedPolylineBean poly = new EncodedPolylineBean();
        poly.setPoints(consequence.getDetourPath());
        detailsBean.setDiversionPath(poly);
      }
      detailsBean.setDiversionStopIds(consequence.getDetourStopIds());
      bean.setConditionDetails(detailsBean);
    }
    return bean;
  }

  public AgencyWithCoverageV2Bean getAgencyWithCoverage(
      AgencyWithCoverageBean awc) {

    AgencyWithCoverageV2Bean bean = new AgencyWithCoverageV2Bean();

    bean.setAgencyId(awc.getAgency().getId());
    bean.setLat(awc.getLat());
    bean.setLon(awc.getLon());
    bean.setLatSpan(awc.getLatSpan());
    bean.setLonSpan(awc.getLonSpan());

    addToReferences(awc.getAgency());

    return bean;
  }

  public NaturalLanguageStringV2Bean getBestString(
      List<NaturalLanguageStringBean> strings) {
    if (strings == null || strings.isEmpty())
      return null;
    NaturalLanguageStringBean noLang = null;
    for (NaturalLanguageStringBean nls : strings) {
      String lang = nls.getLang();
      if (lang == null) {
        noLang = nls;
        continue;
      }
      /**
       * To better match the language, we let Locale handle canonicalization
       */
      Locale locale = new Locale(lang);
      if (locale.getLanguage().equals(_locale.getLanguage()))
        return getString(nls);
    }

    if (noLang != null)
      return getString(noLang);

    return null;
  }

  public NaturalLanguageStringV2Bean getString(NaturalLanguageStringBean nls) {
    if (nls == null)
      return null;
    if (nls.getValue() == null || nls.getValue().isEmpty())
      return null;
    NaturalLanguageStringV2Bean bean = new NaturalLanguageStringV2Bean();
    bean.setLang(nls.getLang());
    bean.setValue(nls.getValue());
    return bean;
  }

  public TimeRangeV2Bean getTimeRange(TimeRangeBean range) {
    if (range == null)
      return null;
    TimeRangeV2Bean bean = new TimeRangeV2Bean();
    bean.setFrom(range.getFrom());
    bean.setTo(range.getTo());
    return bean;
  }

  public CoordinatePointV2Bean getPoint(CoordinatePoint point) {
    if (point == null)
      return null;
    CoordinatePointV2Bean bean = new CoordinatePointV2Bean();
    bean.setLat(point.getLat());
    bean.setLon(point.getLon());
    return bean;
  }

  public CoordinatePoint reversePoint(CoordinatePointV2Bean bean) {
    if (bean == null)
      return null;
    return new CoordinatePoint(bean.getLat(), bean.getLon());
  }

  /****
   * References Methods
   ****/

  public void addToReferences(AgencyBean agency) {
    if (!shouldAddReferenceWithId(_references.getAgencies(), agency.getId()))
      return;
    AgencyV2Bean bean = getAgency(agency);
    _references.addAgency(bean);
  }

  public void addToReferences(RouteBean route) {
    if (!shouldAddReferenceWithId(_references.getRoutes(), route.getId()))
      return;
    RouteV2Bean bean = getRoute(route);
    _references.addRoute(bean);
  }

  public void addToReferences(StopBean stop) {
    if (!shouldAddReferenceWithId(_references.getStops(), stop.getId()))
      return;
    StopV2Bean bean = getStop(stop);
    _references.addStop(bean);
  }

  public void addToReferences(TripBean trip) {
    if (!shouldAddReferenceWithId(_references.getTrips(), trip.getId()))
      return;
    TripV2Bean bean = getTrip(trip);
    _references.addTrip(bean);
  }

  public void addToReferences(ServiceAlertBean situation) {
    if (isSituationExcludedForApplication(situation))
      return;
    if (!shouldAddReferenceWithId(_references.getSituations(),
        situation.getId()))
      return;
    SituationV2Bean bean = getSituation(situation);
    _references.addSituation(bean);
  }

  /****
   * Private Methods
   ****/

  public <T> EntryWithReferencesBean<T> entry(T entry) {
    return new EntryWithReferencesBean<T>(entry, _references);
  }

  public <T> ListWithReferencesBean<T> list(List<T> list, boolean limitExceeded) {
    return new ListWithReferencesBean<T>(list, limitExceeded, _references);
  }

  public <T> ListWithReferencesBean<T> list(List<T> list,
      boolean limitExceeded, boolean outOfRange) {
    return new ListWithRangeAndReferencesBean<T>(list, limitExceeded,
        outOfRange, _references);
  }

  public boolean isStringSet(String value) {
    return value != null && !value.isEmpty();
  }

  private <T> List<T> filter(List<T> beans) {
    if (_maxCount == null)
      return beans;
    return _maxCount.filter(beans, false);
  }

  private <T extends HasId> boolean shouldAddReferenceWithId(
      Iterable<T> entities, String id) {

    if (!_includeReferences)
      return false;

    if (entities == null)
      return true;

    for (T entity : entities) {
      if (entity.getId().equals(id))
        return false;
    }

    return true;
  }

  private Properties getGitProperties(){
          Properties properties = new Properties();
          try {
                  InputStream inputStream = getClass().getClassLoader().getResourceAsStream("git.properties");
                  if (inputStream != null) {
                          properties.load(inputStream);
                  }
                  return properties;
          } catch (IOException ioe) {
                  return null;
          }
  }
}
