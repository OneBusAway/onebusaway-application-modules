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
package org.onebusaway.alerts.service;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.alerts.impl.ServiceAlertRecord;

import java.util.List;

public interface ServiceAlertsService {

  /**
   * Create a service alert. To assist with data federation in the
   * {@link TransitDataService}, each service alert is assigned to an agency, as
   * determined by the 'agencyId' parameter. This doesn't mean that the service
   * alert is 'active' for the agency (as returned by
   * {@link #getServiceAlertsForAgencyId(long, String)}) but it will be returned
   * in a call to {@link #getServiceAlertsForFederatedAgencyId(String)}. This
   * also determines the agency id used in the service alerts id (
   * {@link ServiceAlert#getId()}).
   * 
   * @param builder the filled-in service alert builder
   * @param defaultAgencyId the agency to assign the service alert to
   * 
   * @return the built service alert
   */

  void cleanup();

  void loadServiceAlerts();

  public ServiceAlertRecord createOrUpdateServiceAlert(ServiceAlertRecord serviceAlertRecord);

  public void removeServiceAlert(AgencyAndId serviceAlertId);
  
  public ServiceAlertRecord copyServiceAlert(ServiceAlertRecord serviceAlertRecord);

  public void removeServiceAlerts(List<AgencyAndId> serviceAlertIds);

  /**
   * Remove all service alerts with the specified agency id. This would remove
   * all the service alerts returned by a call to
   * {@link #getServiceAlertsForFederatedAgencyId(String)}.
   * 
   * @param agencyId
   */
  public void removeAllServiceAlertsForFederatedAgencyId(String agencyId);

  public ServiceAlertRecord getServiceAlertForId(AgencyAndId serviceAlertId);

  public List<ServiceAlertRecord> getAllServiceAlerts();

  /**
   * This returns all the service alerts with the specified federated agency id,
   * as set in {@link ServiceAlert#getId()} and in the call to
   * {@link #createServiceAlert(String, org.onebusaway.alerts.service.ServiceAlertService.ServiceAlert.Builder)}
   * . Contrast this with {@link #getServiceAlertsForAgencyId(long, String)},
   * which find service alerts affecting a particular agency.
   * 
   * @param agencyId
   * @return
   */
  public List<ServiceAlertRecord> getServiceAlertsForFederatedAgencyId(String agencyId);

  /**
   * This returns the set of service alerts affecting a particular agency, as
   * determined by {@link Affects#getAgencyId()}.
   * 
   * @param time
   * @param agencyId
   * @return the set of service alerts affecting the specified agency
   */
  public List<ServiceAlertRecord> getServiceAlertsForAgencyId(long time,
      String agencyId);

  public List<ServiceAlertRecord> getServiceAlertsForStopId(long time,
      AgencyAndId stopId);
  
  List<ServiceAlertRecord> getServiceAlertsForRouteId(long time, AgencyAndId routeId);

  List<ServiceAlertRecord> getServiceAlertsForRouteAndStopId(long time, AgencyAndId routeId, AgencyAndId stopId);

  List<ServiceAlertRecord> getServiceAlertsForTripAndStopId(long time, AgencyAndId tripId, AgencyAndId stopId);

  List<ServiceAlertRecord> getServiceAlertsForRouteAndDirection(long time, AgencyAndId routeId, AgencyAndId stopId, String directionId);

  public List<ServiceAlertRecord> getServiceAlerts(SituationQueryBean query);

  List<ServiceAlertRecord> createOrUpdateServiceAlerts(String agencyId, List<ServiceAlertRecord> records);
}
