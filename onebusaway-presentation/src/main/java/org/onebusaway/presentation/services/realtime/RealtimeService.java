/**
 * Copyright (C) 2010 OpenPlans
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
package org.onebusaway.presentation.services.realtime;

import java.util.List;

import org.onebusaway.transit_data_federation.siri.SiriJsonSerializer;
import org.onebusaway.transit_data_federation.siri.SiriXmlSerializer;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;

import uk.org.siri.siri.MonitoredStopVisitStructure;
import uk.org.siri.siri.VehicleActivityStructure;

public interface RealtimeService {

  public void setTime(long time);
  

  public PresentationService getPresentationService();

  public SiriJsonSerializer getSiriJsonSerializer();
  
  public SiriXmlSerializer getSiriXmlSerializer();
  
  
  public VehicleActivityStructure getVehicleActivityForVehicle(String vehicleId, 
      int maximumOnwardCalls, long currentTime, String tripId);

  public List<VehicleActivityStructure> getVehicleActivityForRoute(String routeId, 
	      String directionId, int maximumOnwardCalls, long currentTime, boolean showRawLocation);

  public List<MonitoredStopVisitStructure> getMonitoredStopVisitsForStop(String stopId, 
      int maximumOnwardCalls, long currentTime);  

  
  public boolean getVehiclesInServiceForRoute(String routeId, String directionId, long currentTime);

  public boolean getVehiclesInServiceForStopAndRoute(String stopId, String routeId, long currentTime);

  
  // FIXME TODO: refactor these to receive a passed in collection of MonitoredStopVisits or VehicleActivities?
  public List<ServiceAlertBean> getServiceAlertsForRoute(String routeId);

  public List<ServiceAlertBean> getServiceAlertsForRouteAndDirection(
      String routeId, String directionId);

  public List<ServiceAlertBean> getServiceAlertsForRouteAndStop(
          String routeId, String stopId);

    public List<ServiceAlertBean> getServiceAlertsForAgency(String agencyId);
  
  public List<ServiceAlertBean> getServiceAlertsGlobal();

  boolean showApc();
}