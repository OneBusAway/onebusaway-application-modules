/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.transit_data_federation.services.beans;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.service_alerts.SituationQueryBean;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public interface ServiceAlertsBeanService {

  public ServiceAlertBean createServiceAlert(String agencyId,
      ServiceAlertBean serviceAlert);

  public void updateServiceAlert(ServiceAlertBean serviceAlert);

  public void removeServiceAlert(AgencyAndId serviceAlertId);

  public ServiceAlertBean getServiceAlertForId(AgencyAndId serviceAlertId);

  public List<ServiceAlertBean> getServiceAlertsForFederatedAgencyId(
      String agencyId);

  public void removeAllServiceAlertsForFederatedAgencyId(String agencyId);

  public List<ServiceAlertBean> getServiceAlertsForStopId(long time,
      AgencyAndId stopId);

  public List<ServiceAlertBean> getServiceAlertsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId);

  public List<ServiceAlertBean> getServiceAlertsForVehicleJourney(long time,
      BlockInstance blockInstance, BlockTripEntry blockTrip,
      AgencyAndId vehicleId);

  public List<ServiceAlertBean> getServiceAlerts(SituationQueryBean query);
}
