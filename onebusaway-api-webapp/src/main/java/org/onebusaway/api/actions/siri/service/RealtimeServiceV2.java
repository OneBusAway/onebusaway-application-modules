/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.api.actions.siri.service;

import java.util.List;
import java.util.Map;

import org.onebusaway.api.actions.siri.impl.SiriSupportV2.Filters;
import org.onebusaway.api.actions.siri.model.DetailLevel;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
//import org.onebusaway.nyc.siri.support.SiriJsonSerializerV2;
//import org.onebusaway.nyc.siri.support.SiriXmlSerializerV2;
import org.onebusaway.presentation.services.realtime.PresentationService;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data_federation.siri.SiriJsonSerializerV2;
import org.onebusaway.transit_data_federation.siri.SiriXmlSerializerV2;

import uk.org.siri.siri_2.AnnotatedLineStructure;
import uk.org.siri.siri_2.AnnotatedStopPointStructure;
import uk.org.siri.siri_2.MonitoredStopVisitStructure;
import uk.org.siri.siri_2.VehicleActivityStructure;

public interface RealtimeServiceV2 {


  public void setTime(long time);

  public PresentationService getPresentationService();
  
  public SiriJsonSerializerV2 getSiriJsonSerializer();

  public SiriXmlSerializerV2 getSiriXmlSerializer();

  public VehicleActivityStructure getVehicleActivityForVehicle(
      String vehicleId, int maximumOnwardCalls, DetailLevel detailLevel, 
      long currentTime);

  public List<VehicleActivityStructure> getVehicleActivityForRoute(
      String routeId, String directionId, int maximumOnwardCalls,
      DetailLevel detailLevel, long currentTime);

  public List<MonitoredStopVisitStructure> getMonitoredStopVisitsForStop(
      String stopId, int maximumOnwardCalls, DetailLevel detailLevel,
      long currentTime, List<AgencyAndId> routeIds,
      Map<Filters, String> filters);

  public boolean getVehiclesInServiceForRoute(String routeId,
      String directionId, long currentTime);
/*
  public boolean getVehiclesInServiceForStopAndRoute(String stopId,
      String routeId, long currentTime);

  // FIXME TODO: refactor these to receive a passed in collection of
  // MonitoredStopVisits or VehicleActivities?
  public List<ServiceAlertBean> getServiceAlertsForRoute(String routeId);

  public List<ServiceAlertBean> getServiceAlertsForRouteAndDirection(
      String routeId, String directionId);
*/
  public List<ServiceAlertBean> getServiceAlertsGlobal();

  public Map<Boolean, List<AnnotatedStopPointStructure>> getAnnotatedStopPointStructures(
      List<String> agencyIds, List<AgencyAndId> routeIds,
      DetailLevel detailLevel, long currentTime,
      Map<Filters, String> filters);

  public Map<Boolean, List<AnnotatedStopPointStructure>> getAnnotatedStopPointStructures(
      CoordinateBounds bounds, List<String> agencyIds, List<AgencyAndId> routeIds, DetailLevel detailLevel,
      long responseTimestamp, Map<Filters, String> filters);

  public Map<Boolean, List<AnnotatedLineStructure>> getAnnotatedLineStructures(
      List<String> agencyIds, List<AgencyAndId> routeIds, DetailLevel detailLevel,
      long responseTimestamp, Map<Filters, String> filters);

  public Map<Boolean, List<AnnotatedLineStructure>> getAnnotatedLineStructures(
      List<String> agencyIds, CoordinateBounds bounds, DetailLevel detailLevel,
      long responseTimestamp, Map<Filters, String> filters);

}
