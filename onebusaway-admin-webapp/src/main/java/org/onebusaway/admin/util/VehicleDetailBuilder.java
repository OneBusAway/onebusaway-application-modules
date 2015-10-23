/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.util;

import org.onebusaway.admin.model.json.VehicleLastKnownRecord;
import org.onebusaway.admin.model.json.VehiclePullout;
import org.onebusaway.admin.model.ui.VehicleDetail;

public class VehicleDetailBuilder {

  public VehicleDetail buildVehicleDetail(VehiclePullout pullout,
      VehicleLastKnownRecord lastKnownRecord,
      String headSign,
      String inferredHeadSign) {
    VehicleDetail vehicleDetail = new VehicleDetail();
    vehicleDetail.setVehicleId(lastKnownRecord.getVehicleId());
    vehicleDetail.setLocation(lastKnownRecord.getLatitude() + ", " + lastKnownRecord.getLongitude());
    vehicleDetail.setDirection(Math.abs(lastKnownRecord.getDirection()) % 360);
    vehicleDetail.setDepot(lastKnownRecord.getDepotId());
    vehicleDetail.setHeadSign(headSign);
    vehicleDetail.setInferredHeadSign(inferredHeadSign);
    vehicleDetail.setServiceDate(lastKnownRecord.getServiceDate());
    vehicleDetail.setOperatorId(lastKnownRecord.getOperatorIdDesignator());
    vehicleDetail.setAgency(lastKnownRecord.getAgencyId());
    vehicleDetail.setObservedRunId(lastKnownRecord.getRouteIdDesignator() + "-" + lastKnownRecord.getRunIdDesignator());
    vehicleDetail.setUtsRunId(pullout!=null?pullout.getRun():null);
    vehicleDetail.setInferredRunId(lastKnownRecord.getInferredRunId());
    if (lastKnownRecord.getScheduleDeviation() != null) {
      vehicleDetail.setScheduleDeviation(lastKnownRecord.getScheduleDeviation());
    }
    vehicleDetail.setTripId(lastKnownRecord.getInferredTripId());
    return vehicleDetail;
  }

}
