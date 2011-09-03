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
package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public final class SituationAffectsBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<SituationAffectedAgencyBean> agencies;

  private List<SituationAffectedStopBean> stops;

  private List<SituationAffectedVehicleJourneyBean> vehicleJourneys;

  private List<SituationAffectedApplicationBean> applications;

  public List<SituationAffectedAgencyBean> getAgencies() {
    return agencies;
  }

  public void setAgencies(List<SituationAffectedAgencyBean> agencies) {
    this.agencies = agencies;
  }

  public List<SituationAffectedStopBean> getStops() {
    return stops;
  }

  public void setStops(List<SituationAffectedStopBean> stops) {
    this.stops = stops;
  }

  public List<SituationAffectedVehicleJourneyBean> getVehicleJourneys() {
    return vehicleJourneys;
  }

  public void setVehicleJourneys(
      List<SituationAffectedVehicleJourneyBean> vehicleJourneys) {
    this.vehicleJourneys = vehicleJourneys;
  }

  public List<SituationAffectedApplicationBean> getApplications() {
    return applications;
  }

  public void setApplications(
      List<SituationAffectedApplicationBean> applications) {
    this.applications = applications;
  }
}
