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

import com.opensymphony.xwork2.ActionInvocation;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.mapper.ClassAliasingMapper;
import org.apache.struts2.rest.handler.XStreamHandler;

import org.onebusaway.api.actions.api.ValidationErrorBean;
import org.onebusaway.api.model.ResponseBean;
import org.onebusaway.api.model.TimeBean;
import org.onebusaway.api.model.transit.*;
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
import org.onebusaway.api.model.where.ArrivalAndDepartureBeanV1;
import org.onebusaway.geospatial.model.EncodedPolygonBean;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ArrivalAndDepartureBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopCalendarDaysBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;

import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class CustomXStreamHandler extends XStreamHandler {

  @Override
  public String fromObject(ActionInvocation invocation, Object obj, String resultCode, Writer out) throws IOException {
    if (obj != null) {
      if (obj instanceof ResponseBean) {
        ResponseBean bean = (ResponseBean) obj;
        if (bean.isString() && bean.getData() != null) {
          out.write(bean.getData().toString());
          return null;
        }
        XStream xstream = this.createXStream(invocation);
        xstream.toXML(obj, out);
      }

    }

    return null;
  }

  @Override
  public void toObject(ActionInvocation invocation, Reader in, Object target) {
    XStream xstream = this.createXStream(invocation);
    xstream.fromXML(in, target);
  }

  @Override
  protected XStream createXStream(ActionInvocation invocation) {
    return createXStream();
  }

  @Override
  protected XStream createXStream() {
    XStream xstream = super.createXStream();
    xstream.setMode(XStream.NO_REFERENCES);
    xstream.alias("response", ResponseBean.class);
    xstream.omitField(ResponseBean.class, "isText");
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

    xstream.alias("currentVehicleEstimate", CurrentVehicleEstimateV2Bean.class);
    
    xstream.alias("registeredAlarm", RegisteredAlarmV2Bean.class);

    xstream.alias("tripWithStopTimes", TripWithStopTimesV2Bean.class);


    // serialization customizations for StopsAndTripsForDirectionV2Bean
    xstream.alias("scheduleStopTime", ScheduleStopTimeInstanceExtendedWithStopIdV2Bean.class);
    ClassAliasingMapper stopTimeExtendedWithStopMapper = new ClassAliasingMapper(xstream.getMapper());
    stopTimeExtendedWithStopMapper.addClassAlias("stopId", String.class);

    // serialization customizations for StopsAndTripsForDirectionV2Bean
    ClassAliasingMapper StopTripDirectionMapper = new ClassAliasingMapper(xstream.getMapper());
    StopTripDirectionMapper.addClassAlias("stopId", String.class);
    xstream.registerLocalConverter(StopsAndTripsForDirectionV2Bean.class, "stopIds",
            new CollectionConverter(StopTripDirectionMapper));
    StopTripDirectionMapper = new ClassAliasingMapper(xstream.getMapper());
    StopTripDirectionMapper.addClassAlias("tripId", String.class);
    xstream.registerLocalConverter(StopsAndTripsForDirectionV2Bean.class, "tripIds",
            new CollectionConverter(StopTripDirectionMapper));

    //serialization customizations for RouteScheduleV2Bean
    xstream.alias("routeSchedule", RouteScheduleV2Bean.class);
    xstream.alias("stopTripGrouping", StopsAndTripsForDirectionV2Bean.class);
    ClassAliasingMapper routeScheduleMapper = new ClassAliasingMapper(xstream.getMapper());
    routeScheduleMapper.addClassAlias("serviceId", String.class);
    xstream.registerLocalConverter(RouteScheduleV2Bean.class, "serviceIds",
            new CollectionConverter(routeScheduleMapper));

    // serialization customizations for StopV2Bean
    ClassAliasingMapper StopMapper = new ClassAliasingMapper(xstream.getMapper());
    StopMapper.addClassAlias("routeId", String.class);
    xstream.registerLocalConverter(StopV2Bean.class, "routeIds",
            new CollectionConverter(StopMapper));



    return xstream;
  }
}
