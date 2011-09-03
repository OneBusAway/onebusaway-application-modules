/**
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
package org.onebusaway.transit_data_federation.services.service_alerts;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.blocks.BlockInstance;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockStopTimeEntry;
import org.onebusaway.transit_data_federation.services.transit_graph.BlockTripEntry;

public interface ServiceAlertsService {

  public Situation createServiceAlert(String agencyId, Situation situation);

  public void updateServiceAlert(Situation situation);

  public void updateServiceAlerts(List<Situation> situations);

  public void removeServiceAlert(AgencyAndId situationId);

  public void removeServiceAlerts(List<AgencyAndId> situationIds);

  public Situation getServiceAlertForId(AgencyAndId situationId);
  
  public List<Situation> getAllSituations();

  public List<Situation> getAllSituationsForAgencyId(String agencyId);

  public void removeAllSituationsForAgencyId(String agencyId);

  public List<Situation> getSituationsForStopId(long time, AgencyAndId stopId);

  public List<Situation> getSituationsForStopCall(long time,
      BlockInstance blockInstance, BlockStopTimeEntry blockStopTime,
      AgencyAndId vehicleId);

  public List<Situation> getSituationsForVehicleJourney(long time,
      BlockInstance blockInstance, BlockTripEntry blockTrip,
      AgencyAndId vehicleId);

}
