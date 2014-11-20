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
package org.onebusaway.api.impl;

import org.apache.struts2.rest.handler.XStreamHandler;
import org.onebusaway.api.actions.api.ValidationErrorBean;
import org.onebusaway.api.model.GitRepositoryStateV2Bean;
import org.onebusaway.api.model.InstanceDetailsV2Bean;
import org.onebusaway.api.model.InstanceVersionsV2Bean;
import org.onebusaway.api.model.InstancesV2Bean;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.TimeBean;
import org.onebusaway.api.model.transit.AgencyV2Bean;
import org.onebusaway.api.model.transit.AgencyWithCoverageV2Bean;
import org.onebusaway.api.model.transit.ArrivalAndDepartureV2Bean;
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.api.model.transit.ListWithRangeAndReferencesBean;
import org.onebusaway.api.model.transit.ListWithReferencesBean;
import org.onebusaway.api.model.transit.ReferencesBean;
import org.onebusaway.api.model.transit.RegisteredAlarmV2Bean;
import org.onebusaway.api.model.transit.RouteV2Bean;
import org.onebusaway.api.model.transit.ScheduleFrequencyInstanceV2Bean;
import org.onebusaway.api.model.transit.ScheduleStopTimeInstanceV2Bean;
import org.onebusaway.api.model.transit.StopCalendarDayV2Bean;
import org.onebusaway.api.model.transit.StopRouteDirectionScheduleV2Bean;
import org.onebusaway.api.model.transit.StopRouteScheduleV2Bean;
import org.onebusaway.api.model.transit.StopScheduleV2Bean;
import org.onebusaway.api.model.transit.StopV2Bean;
import org.onebusaway.api.model.transit.StopWithArrivalsAndDeparturesV2Bean;
import org.onebusaway.api.model.transit.StopsForRouteV2Bean;
import org.onebusaway.api.model.transit.TimeIntervalV2;
import org.onebusaway.api.model.transit.TripDetailsV2Bean;
import org.onebusaway.api.model.transit.TripStopTimeV2Bean;
import org.onebusaway.api.model.transit.TripV2Bean;
import org.onebusaway.api.model.transit.VehicleLocationRecordV2Bean;
import org.onebusaway.api.model.transit.VehicleStatusV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockConfigurationV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockInstanceV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockStopTimeV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockTripV2Bean;
import org.onebusaway.api.model.transit.blocks.BlockV2Bean;
import org.onebusaway.api.model.transit.realtime.CurrentVehicleEstimateV2Bean;
import org.onebusaway.api.model.transit.schedule.StopTimeV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationAffectedAgencyV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationAffectedApplicationV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationAffectedCallV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationAffectedStopV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationAffectedVehicleJourneyV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationAffectsV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationConditionDetailsV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationConsequenceV2Bean;
import org.onebusaway.api.model.transit.service_alerts.SituationV2Bean;
import org.onebusaway.api.model.transit.service_alerts.TimeRangeV2Bean;
import org.onebusaway.api.model.transit.tripplanning.EdgeV2Bean;
import org.onebusaway.api.model.transit.tripplanning.GraphResultV2Bean;
import org.onebusaway.api.model.transit.tripplanning.ItinerariesV2Bean;
import org.onebusaway.api.model.transit.tripplanning.ItineraryV2Bean;
import org.onebusaway.api.model.transit.tripplanning.LegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.LocationV2Bean;
import org.onebusaway.api.model.transit.tripplanning.MinTravelTimeToStopV2Bean;
import org.onebusaway.api.model.transit.tripplanning.StreetLegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.TransitLegV2Bean;
import org.onebusaway.api.model.transit.tripplanning.VertexV2Bean;
import org.onebusaway.api.model.where.ArrivalAndDepartureBeanV1;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.siri.model.ErrorMessage;
import org.onebusaway.siri.model.MonitoredStopVisit;
import org.onebusaway.siri.model.ServiceRequestContext;
import org.onebusaway.siri.model.Siri;
import org.onebusaway.siri.model.VehicleLocation;
import org.onebusaway.siri.model.VehicleMonitoringDetailLevel;
import org.onebusaway.siri.model.VehicleMonitoringRequest;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;

import com.thoughtworks.xstream.XStream;

public class CustomXStreamHandler extends XStreamHandler {

  @Override
  protected XStream createXStream() {
    XStream xstream = super.createXStream();
    xstream.setMode(XStream.NO_REFERENCES);
    xstream.alias("response", ResponseBean.class);
    xstream.alias("validationError", ValidationErrorBean.class);
    xstream.alias("time", TimeBean.class);
    xstream.alias("stop", StopBean.class);
    xstream.alias("route", RouteBean.class);
    xstream.alias("arrivalAndDeparture", ArrivalAndDepartureBean.class);
    xstream.alias("arrivalAndDeparture", ArrivalAndDepartureBeanV1.class);
    xstream.alias("encodedPolyline", EncodedPolylineBean.class);
    xstream.alias("encodedPolygon", EncodedPolygonBean.class);
    xstream.alias("stopGrouping", StopGroupingBean.class);
    xstream.alias("stopGroup", StopGroupBean.class);
    xstream.alias("agency-with-coverage", AgencyWithCoverageBean.class);
    xstream.alias("calendar-days", StopCalendarDaysBean.class);

    xstream.alias("entryWithReferences", EntryWithReferencesBean.class);
    xstream.alias("listWithReferences", ListWithReferencesBean.class);
    xstream.alias("listWithRangeAndReferences",
        ListWithRangeAndReferencesBean.class);
    xstream.alias("references", ReferencesBean.class);

    xstream.alias("agency", AgencyV2Bean.class);
    xstream.alias("route", RouteV2Bean.class);
    xstream.alias("stop", StopV2Bean.class);
    xstream.alias("trip", TripV2Bean.class);
    xstream.alias("tripDetails", TripDetailsV2Bean.class);
    xstream.alias("blockInstance", BlockInstanceV2Bean.class);
    xstream.alias("block", BlockV2Bean.class);
    xstream.alias("blockConfiguration", BlockConfigurationV2Bean.class);
    xstream.alias("blockTrip", BlockTripV2Bean.class);
    xstream.alias("blockStopTime", BlockStopTimeV2Bean.class);
    xstream.alias("stopTime", StopTimeV2Bean.class);
    xstream.alias("tripStopTime", TripStopTimeV2Bean.class);
    xstream.alias("stopSchedule", StopScheduleV2Bean.class);
    xstream.alias("stopRouteSchedule", StopRouteScheduleV2Bean.class);
    xstream.alias("stopRouteDirectionSchedule",
        StopRouteDirectionScheduleV2Bean.class);
    xstream.alias("scheduleStopTime", ScheduleStopTimeInstanceV2Bean.class);
    xstream.alias("scheduleFrequency", ScheduleFrequencyInstanceV2Bean.class);
    xstream.alias("stopCalendarDay", StopCalendarDayV2Bean.class);
    xstream.alias("stopWithArrivalsAndDepartures",
        StopWithArrivalsAndDeparturesV2Bean.class);
    xstream.alias("arrivalAndDeparture", ArrivalAndDepartureV2Bean.class);
    xstream.alias("agencyWithCoverage", AgencyWithCoverageV2Bean.class);
    xstream.alias("stopsForRoute", StopsForRouteV2Bean.class);
    xstream.alias("vehicleLocationRecord", VehicleLocationRecordV2Bean.class);
    xstream.alias("vehicleStatus", VehicleStatusV2Bean.class);

    xstream.alias("situation", SituationV2Bean.class);
    xstream.alias("affects", SituationAffectsV2Bean.class);
    xstream.alias("agency", SituationAffectedAgencyV2Bean.class);
    xstream.alias("stop", SituationAffectedStopV2Bean.class);
    xstream.alias("vehicleJourney", SituationAffectedVehicleJourneyV2Bean.class);
    xstream.alias("call", SituationAffectedCallV2Bean.class);
    xstream.alias("application", SituationAffectedApplicationV2Bean.class);
    xstream.alias("consequence", SituationConsequenceV2Bean.class);
    xstream.alias("conditionDetails", SituationConditionDetailsV2Bean.class);

    xstream.alias("timeRange", TimeRangeV2Bean.class);

    xstream.alias("VehicleLocation", VehicleLocation.class);

    xstream.alias("itineraries", ItinerariesV2Bean.class);
    xstream.alias("itinerary", ItineraryV2Bean.class);
    xstream.alias("location", LocationV2Bean.class);
    xstream.alias("leg", LegV2Bean.class);
    xstream.alias("transitLeg", TransitLegV2Bean.class);
    xstream.alias("streetLeg", StreetLegV2Bean.class);

    xstream.alias("timeInterval", TimeIntervalV2.class);

    xstream.alias("minTravelTimeToStop", MinTravelTimeToStopV2Bean.class);

    xstream.alias("graphResult", GraphResultV2Bean.class);
    xstream.alias("vertex", VertexV2Bean.class);
    xstream.alias("edge", EdgeV2Bean.class);

    xstream.alias("currentVehicleEstimate", CurrentVehicleEstimateV2Bean.class);
    
    xstream.alias("registeredAlarm", RegisteredAlarmV2Bean.class);
    
    xstream.alias("instanceVersions", InstanceVersionsV2Bean.class);
    xstream.alias("version", GitRepositoryStateV2Bean.class);
    
    xstream.alias("instances", InstancesV2Bean.class);
    xstream.alias("instance", InstanceDetailsV2Bean.class);
    
    xstream.processAnnotations(VehicleMonitoringRequest.class);
    xstream.processAnnotations(VehicleMonitoringDetailLevel.class);
    xstream.processAnnotations(ServiceRequestContext.class);
    xstream.processAnnotations(Siri.class);
    xstream.processAnnotations(ErrorMessage.class);
    xstream.processAnnotations(MonitoredStopVisit.class);
    return xstream;
  }
}
