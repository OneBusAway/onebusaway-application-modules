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

package org.onebusaway.nextbus.impl;

import org.apache.struts2.rest.handler.XStreamHandler;
import org.onebusaway.nextbus.model.RouteScheduleInfo;
import org.onebusaway.nextbus.model.Routes;
import org.onebusaway.nextbus.model.StopScheduleInfo;
import org.onebusaway.nextbus.model.StopsResp;

import com.thoughtworks.xstream.XStream;

public class CustomXStreamHandler extends XStreamHandler {

  @Override
  protected XStream createXStream() {
    XStream xstream = super.createXStream();
    xstream.setMode(XStream.NO_REFERENCES);
    xstream.processAnnotations(RouteScheduleInfo.class);
    xstream.processAnnotations(StopsResp.class);
    xstream.processAnnotations(Routes.class);
    xstream.processAnnotations(StopScheduleInfo.class);
    
    /*xstream.alias("response", ResponseBean.class);
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
    
    

    xstream.processAnnotations(VehicleMonitoringRequest.class);
    xstream.processAnnotations(VehicleMonitoringDetailLevel.class);
    xstream.processAnnotations(ServiceRequestContext.class);
    xstream.processAnnotations(Siri.class);
    xstream.processAnnotations(ErrorMessage.class);
    xstream.processAnnotations(MonitoredStopVisit.class);*/
    
    return xstream;
  }
}
