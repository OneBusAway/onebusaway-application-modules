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
package org.onebusaway.alerts.impl;

import org.onebusaway.gtfs.model.AgencyAndId;

import java.util.Map;
import java.util.Set;

public interface ServiceAlertsCache {
  
  void clear();

  Map<AgencyAndId, ServiceAlertRecord> getServiceAlerts();

  ServiceAlertRecord removeServiceAlert(AgencyAndId serviceAlertId);

  ServiceAlertRecord putServiceAlert(AgencyAndId id, ServiceAlertRecord serviceAlert);
  
  Map<String, Set<AgencyAndId>> getServiceAlertIdsByServiceAlertAgencyId();

  Map<String, Set<AgencyAndId>> getServiceAlertIdsByAgencyId();

  Map<AgencyAndId, Set<AgencyAndId>> getServiceAlertIdsByStopId();

  Map<AgencyAndId, Set<AgencyAndId>> getServiceAlertIdsByRouteId();

  Map<RouteAndDirectionRef, Set<AgencyAndId>> getServiceAlertIdsByRouteAndDirectionId();

  Map<RouteAndStopCallRef, Set<AgencyAndId>> getServiceAlertIdsByRouteAndStop();

  Map<RouteDirectionAndStopCallRef, Set<AgencyAndId>> getServiceAlertIdsByRouteDirectionAndStopCall();

  Map<AgencyAndId, Set<AgencyAndId>> getServiceAlertIdsByTripId();

  Map<TripAndStopCallRef, Set<AgencyAndId>> getServiceAlertIdsByTripAndStopId();
  
  

  

}
