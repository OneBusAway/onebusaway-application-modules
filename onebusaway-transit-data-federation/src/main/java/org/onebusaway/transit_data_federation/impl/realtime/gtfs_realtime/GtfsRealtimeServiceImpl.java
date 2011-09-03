/**
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
package org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.VehicleLocationRecord;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.blocks.BlockStatusService;
import org.onebusaway.transit_data_federation.services.realtime.BlockLocation;
import org.onebusaway.transit_data_federation.services.realtime.VehicleStatus;
import org.onebusaway.transit_data_federation.services.realtime.VehicleStatusService;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlertsService;
import org.onebusaway.transit_data_federation.services.service_alerts.Situation;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedAgency;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedCall;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedStop;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffectedVehicleJourney;
import org.onebusaway.transit_data_federation.services.service_alerts.SituationAffects;
import org.onebusaway.transit_data_federation.services.service_alerts.TimeRange;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.StopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.TripEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.EntitySelector;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedHeader.Incrementality;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TranslatedString;
import com.google.transit.realtime.GtfsRealtime.TranslatedString.Translation;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtimeConstants;

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

    List<BlockLocation> activeBlocks = _blockStatusService.getAllActiveBlocks(System.currentTimeMillis());
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

      // No matter what our active trip is, we let our current trip be the the
      // trip of our next stop
      BlockTripEntry activeBlockTrip = nextBlockStop.getTrip();
      TripEntry activeTrip = activeBlockTrip.getTrip();
      StopTimeEntry nextStopTime = nextBlockStop.getStopTime();
      StopEntry stop = nextStopTime.getStop();

      TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();

      StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
      stopTimeUpdate.setStopId(AgencyAndId.convertToString(stop.getId()));
      stopTimeUpdate.setStopSequence(nextStopTime.getSequence());
      stopTimeUpdate.setScheduleRelationship(com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate.ScheduleRelationship.SCHEDULED);
      tripUpdate.addStopTimeUpdate(stopTimeUpdate);

      StopTimeEvent.Builder stopTimeEvent = StopTimeEvent.newBuilder();
      stopTimeEvent.setDelay((int) activeBlock.getScheduleDeviation());
      stopTimeUpdate.setDeparture(stopTimeEvent);

      AgencyAndId routeId = activeTrip.getRouteCollectionId();
      AgencyAndId tripId = activeTrip.getId();
      BlockInstance blockInstance = activeBlock.getBlockInstance();
      String startDate = String.format("%1$ty%1$tm%1$td", new Date(
          blockInstance.getServiceDate()));

      TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
      tripDescriptor.setRouteId(AgencyAndId.convertToString(routeId));
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
        vehiclePosition.setTimestamp(record.getTimeOfLocationUpdate());
      else
        vehiclePosition.setTimestamp(record.getTimeOfRecord());

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
    List<Situation> situations = _serviceAlertsService.getAllSituations();
    for (Situation situation : situations) {

      Alert.Builder alert = Alert.newBuilder();

      if (situation.getSummary() != null) {
        NaturalLanguageStringBean nls = situation.getSummary();
        TranslatedString.Builder translated = getNLSAsTranslatedString(nls);
        alert.setHeaderText(translated);
      }

      if (situation.getDescription() != null) {
        NaturalLanguageStringBean nls = situation.getDescription();
        TranslatedString.Builder translated = getNLSAsTranslatedString(nls);
        alert.setDescriptionText(translated);
      }

      TimeRange range = situation.getPublicationWindow();
      if (range != null) {
        com.google.transit.realtime.GtfsRealtime.TimeRange.Builder timeRange = com.google.transit.realtime.GtfsRealtime.TimeRange.newBuilder();
        if (range.getFrom() != 0)
          timeRange.setStart(range.getFrom());
        if (range.getTo() != 0)
          timeRange.setEnd(range.getTo());
        alert.addActivePeriod(timeRange);
      }

      /**
       * What does this situation affect?
       */
      SituationAffects affects = situation.getAffects();

      for (SituationAffectedAgency agency : list(affects.getAgencies())) {
        EntitySelector.Builder entitySelector = EntitySelector.newBuilder();
        entitySelector.setAgencyId(agency.getAgencyId());
        alert.addInformedEntity(entitySelector);
      }

      for (SituationAffectedVehicleJourney vehicleJourney : list(affects.getVehicleJourneys())) {
        AgencyAndId lineId = vehicleJourney.getLineId();
        for (AgencyAndId tripId : atLeastOneList(vehicleJourney.getTripIds(),
            null)) {
          for (SituationAffectedCall call : atLeastOneList(
              vehicleJourney.getCalls(), null)) {
            if (lineId == null && tripId == null && call == null)
              continue;
            EntitySelector.Builder entitySelector = EntitySelector.newBuilder();
            if (lineId != null)
              entitySelector.setRouteId(AgencyAndId.convertToString(lineId));
            if (tripId != null) {
              TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
              tripDescriptor.setTripId(AgencyAndId.convertToString(tripId));
              entitySelector.setTrip(tripDescriptor);
            }
            if (call != null)
              entitySelector.setStopId(AgencyAndId.convertToString(call.getStopId()));
            alert.addInformedEntity(entitySelector);
          }
        }
      }

      for (SituationAffectedStop stop : list(affects.getStops())) {
        EntitySelector.Builder entitySelector = EntitySelector.newBuilder();
        entitySelector.setStopId(AgencyAndId.convertToString(stop.getStopId()));
        alert.addInformedEntity(entitySelector);
      }

      FeedEntity.Builder feedEntity = FeedEntity.newBuilder();
      feedEntity.setAlert(alert);
      feedEntity.setId(AgencyAndId.convertToString(situation.getId()));
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

  private TranslatedString.Builder getNLSAsTranslatedString(
      NaturalLanguageStringBean nls) {
    TranslatedString.Builder translated = TranslatedString.newBuilder();
    Translation.Builder translation = Translation.newBuilder();
    if (nls.getLang() != null)
      translation.setLanguage(nls.getLang());
    translation.setText(nls.getValue());
    translated.addTranslation(translation);
    return translated;
  }

  private static final <T> List<T> list(List<T> values) {
    if (values == null)
      return Collections.emptyList();
    return values;
  }

  @SuppressWarnings("unchecked")
  private static final <T> List<T> atLeastOneList(List<T> values, T defaultValue) {
    if (values == null || values.isEmpty())
      return Arrays.asList(defaultValue);
    return values;
  }
}
