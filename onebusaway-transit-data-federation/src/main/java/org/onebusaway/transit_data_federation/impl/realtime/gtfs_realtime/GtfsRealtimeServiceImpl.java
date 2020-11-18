/**
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import com.google.transit.realtime.GtfsRealtime.*;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtimeConstants;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.alerts.impl.ServiceAlertLocalizedString;
import org.onebusaway.alerts.impl.ServiceAlertRecord;
import org.onebusaway.alerts.impl.ServiceAlertTimeRange;
import org.onebusaway.alerts.impl.ServiceAlertsSituationAffectsClause;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.VehicleStatus;
import org.onebusaway.transit_data_federation.services.realtime.VehicleStatusService;
import org.onebusaway.alerts.service.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.transit_graph.*;
import org.onebusaway.util.SystemTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
class GtfsRealtimeServiceImpl implements GtfsRealtimeService {

  private VehicleStatusService _vehicleStatusService;

  private BlockStatusService _blockStatusService;

  private ServiceAlertsService _serviceAlertsService;

  @Autowired
  public void setVehicleStatusService(VehicleStatusService vehicleStatusService) {
    _vehicleStatusService = vehicleStatusService;
  }

  @Autowired
  public void setBlockStatusService(BlockStatusService blockStatusService) {
    _blockStatusService = blockStatusService;
  }

  @Autowired
  public void setServiceAlertsService(ServiceAlertsService serviceAlertsService) {
    _serviceAlertsService = serviceAlertsService;
  }

  @Override
  public FeedMessage getTripUpdates() {

    FeedMessage.Builder feedMessage = createFeedWithDefaultHeader();

    List<BlockLocation> activeBlocks = _blockStatusService.getAllActiveBlocks(SystemTime.currentTimeMillis());
    for (BlockLocation activeBlock : activeBlocks) {

      // Only interested in blocks with real-time data
      if (!activeBlock.isPredicted())
        continue;

      // Only interested in blocks with a next stop
      BlockStopTimeEntry nextBlockStop = activeBlock.getNextStop();
      if (nextBlockStop == null)
        continue;

      // Only interested in blocks with a schedule deviation set
      if (!activeBlock.isScheduleDeviationSet())
        continue;

      TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
      BlockTripEntry activeBlockTrip = nextBlockStop.getTrip();
      TripEntry activeTrip = activeBlockTrip.getTrip();
      
      if (activeBlock.getTimepointPredictions() != null && activeBlock.getTimepointPredictions().size() > 0) {
        // If multiple stoptime predictions were originally obtained,
        // pass them through as received
        List<TimepointPredictionRecord> timepointPredictions = activeBlock.getTimepointPredictions();
        
        for (TimepointPredictionRecord tpr: timepointPredictions) {
           StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
           stopTimeUpdate.setStopId(AgencyAndId.convertToString(tpr.getTimepointId()));
           stopTimeUpdate.setScheduleRelationship(StopTimeUpdate.ScheduleRelationship.valueOf(tpr.getScheduleRelationship().getValue()));
      
           if (tpr.getTimepointPredictedArrivalTime() != -1) {
             StopTimeEvent.Builder arrivalStopTimeEvent = StopTimeEvent.newBuilder();
             arrivalStopTimeEvent.setTime(tpr.getTimepointPredictedArrivalTime());
             stopTimeUpdate.setArrival(arrivalStopTimeEvent);
           }
   
           if (tpr.getTimepointPredictedDepartureTime() != -1) {
             StopTimeEvent.Builder departureStopTimeEvent = StopTimeEvent.newBuilder();
             departureStopTimeEvent.setTime(tpr.getTimepointPredictedDepartureTime());
             stopTimeUpdate.setDeparture(departureStopTimeEvent);
           }
   
           tripUpdate.addStopTimeUpdate(stopTimeUpdate); 
        }
      } else {
        // No matter what our active trip is, we let our current trip be the the
        // trip of our next stop
        StopTimeEntry nextStopTime = nextBlockStop.getStopTime();
        StopEntry stop = nextStopTime.getStop();
      
        StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
        stopTimeUpdate.setStopId(AgencyAndId.convertToString(stop.getId()));
        stopTimeUpdate.setStopSequence(nextStopTime.getSequence());
        stopTimeUpdate.setScheduleRelationship(com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate.ScheduleRelationship.SCHEDULED);
      
        StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
        stopTimeEvent.setDelay((int) activeBlock.getScheduleDeviation());
        stopTimeUpdate.setDeparture(stopTimeEvent);
   
        tripUpdate.addStopTimeUpdate(stopTimeUpdate);
      }
      
      AgencyAndId routeId = activeTrip.getRouteCollection().getId();
      AgencyAndId tripId = activeTrip.getId();
      BlockInstance blockInstance = activeBlock.getBlockInstance();
      String startDate = String.format("%1$ty%1$tm%1$td", new Date(
          blockInstance.getServiceDate()));

      TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
      tripDescriptor.setRouteId(AgencyAndId.convertToString(routeId));
      if (activeBlock.getStatus() != null)
        tripDescriptor.setScheduleRelationship(com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship.valueOf(activeBlock.getStatus()));
      else
        tripDescriptor.setScheduleRelationship(ScheduleRelationship.SCHEDULED);
      tripDescriptor.setStartDate(startDate);
      tripDescriptor.setTripId(AgencyAndId.convertToString(tripId));
      tripUpdate.setTrip(tripDescriptor);

      AgencyAndId vehicleId = activeBlock.getVehicleId();

      VehicleDescriptor.Builder vehicleDescriptor = VehicleDescriptor.newBuilder();
      vehicleDescriptor.setId(AgencyAndId.convertToString(vehicleId));
      tripUpdate.setVehicle(vehicleDescriptor);

      FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
      feedEntity.setTripUpdate(tripUpdate);
      feedEntity.setId(vehicleDescriptor.getId());
      feedMessage.addEntity(feedEntity);
    }
    return feedMessage.build();
  }

  @Override
  public FeedMessage getVehiclePositions() {

    FeedMessage.Builder feedMessage = createFeedWithDefaultHeader();

    List<VehicleStatus> statuses = _vehicleStatusService.getAllVehicleStatuses();
    for (VehicleStatus status : statuses) {

      VehicleLocationRecord record = status.getRecord();
      VehiclePosition.Builder vehiclePosition = VehiclePosition.newBuilder();

      if (record.isCurrentLocationSet()) {
        Position.Builder position = Position.newBuilder();
        position.setLatitude((float) record.getCurrentLocationLat());
        position.setLongitude((float) record.getCurrentLocationLon());
        vehiclePosition.setPosition(position);
      }

      VehicleDescriptor.Builder vehicleDescriptor = VehicleDescriptor.newBuilder();
      vehicleDescriptor.setId(AgencyAndId.convertToString(record.getVehicleId()));
      vehiclePosition.setVehicle(vehicleDescriptor);

      if (record.getTimeOfLocationUpdate() != 0)
        vehiclePosition.setTimestamp(record.getTimeOfLocationUpdate() / 1000);
      else
        vehiclePosition.setTimestamp(record.getTimeOfRecord() / 1000);

      /**
       * TODO: Block? Trip?
       */

      FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
      feedEntity.setVehicle(vehiclePosition);
      feedEntity.setId(vehicleDescriptor.getId());
      feedMessage.addEntity(feedEntity);
    }

    return feedMessage.build();
  }

  @Override
  public FeedMessage getAlerts() {
    FeedMessage.Builder feedMessage = createFeedWithDefaultHeader();
    List<ServiceAlertRecord> serviceAlerts = _serviceAlertsService.getAllServiceAlerts();
    for (ServiceAlertRecord serviceAlert : serviceAlerts) {

      Alert.Builder alert = Alert.newBuilder();

      if (serviceAlert.getSummaries() != null) {
        for(ServiceAlertLocalizedString string : serviceAlert.getSummaries()){
          TranslatedString translated = convertTranslatedString(string);
          alert.setHeaderText(translated);
        }
      }

      if (serviceAlert.getDescriptions() != null) {
        for(ServiceAlertLocalizedString string : serviceAlert.getDescriptions()){
          TranslatedString translated = convertTranslatedString(string);
          alert.setDescriptionText(translated);
        }
      }

      for (ServiceAlertTimeRange range : serviceAlert.getActiveWindows()) {
        com.google.transit.realtime.GtfsRealtime.TimeRange.Builder timeRange = com.google.transit.realtime.GtfsRealtime.TimeRange.newBuilder();
        if (range.getFromValue() != null)
          timeRange.setStart(range.getFromValue());
        if (range.getToValue() != null)
          timeRange.setEnd(range.getToValue());
        alert.addActivePeriod(timeRange);
      }

      /**
       * What does this situation affect?
       */
      for (ServiceAlertsSituationAffectsClause affects : serviceAlert.getAllAffects()) {
        EntitySelector.Builder entitySelector = EntitySelector.newBuilder();
        if (affects.getAgencyId() != null)
          entitySelector.setAgencyId(affects.getAgencyId());
        if (affects.getRouteId() != null)
          entitySelector.setRouteId(id(serviceAlert.getAgencyId(), affects.getRouteId()));
        if (affects.getTripId() != null) {
          TripDescriptor.Builder trip = TripDescriptor.newBuilder();
          trip.setTripId(id(serviceAlert.getAgencyId(), affects.getTripId()));
          entitySelector.setTrip(trip);
        }
        if (affects.getStopId() != null)
          entitySelector.setStopId(
              id(serviceAlert.getAgencyId(), affects.getStopId()));
        alert.addInformedEntity(entitySelector);
      }

      FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
      feedEntity.setAlert(alert);
      feedEntity.setId(id(serviceAlert.getAgencyId(), serviceAlert.getServiceAlertId()));
      feedMessage.addEntity(feedEntity);
    }
    return feedMessage.build();
  }

  /****
   * Private Methods
   ****/

  private FeedMessage.Builder createFeedWithDefaultHeader() {
    FeedMessage.Builder feedMessage = FeedMessage.newBuilder();

    FeedHeader.Builder feedHeader = FeedHeader.newBuilder();
    feedHeader.setIncrementality(Incrementality.FULL_DATASET);
    feedHeader.setGtfsRealtimeVersion(GtfsRealtimeConstants.VERSION);
    feedMessage.setHeader(feedHeader);
    return feedMessage;
  }

  private TranslatedString convertTranslatedString(
      ServiceAlertLocalizedString ts) {
    TranslatedString.Builder translated = TranslatedString.newBuilder();

    Translation.Builder builder = Translation.newBuilder();
    builder.setText(ts.getValue());
    if (ts.getLanguage() != null)
      builder.setLanguage(ts.getLanguage());
    translated.addTranslation(builder);
    return translated.build();
  }

  private String id(String agencyId, String id) {
    return AgencyAndId.convertToString(new AgencyAndId(agencyId, id));
  }
}
