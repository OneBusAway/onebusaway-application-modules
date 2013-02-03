/**
 * Copyright (C) 2013 Kurt Raschke
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
package org.onebusaway.transit_data_federation.impl.predictions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.TripStopTimeBean;
import org.onebusaway.transit_data.model.trips.TripDetailsBean;
import org.onebusaway.transit_data.model.trips.TripDetailsInclusionBean;
import org.onebusaway.transit_data.model.trips.TripDetailsQueryBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.beans.TripDetailsBeanService;
import org.onebusaway.transit_data_federation.services.predictions.PredictionIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implement PredictionIntegrationService using the per-trip schedule deviation
 * provided to the TransitDataService from an external (SIRI, GTFS-realtime,
 * OrbCAD) source.
 *
 * Fakes per-stop predictions by applying the same deviation to every stoptime
 * in the trip.
 *
 *
 * @author kurt
 */
@Component
public class ScheduleDeviationPredictionIntegrationServiceImpl implements PredictionIntegrationService {

    @Autowired
    private TripDetailsBeanService _tripDetailsBeanService;

    @Override
    public void updatePredictionsForVehicle(AgencyAndId vehicleId) {
        //no-op because we don't have a PredictionGenerationService to
        //cache results from.
    }

    @Override
    public List<TimepointPredictionRecord> getPredictionsForTrip(TripStatusBean tripStatus) {
        List<TimepointPredictionRecord> tprs = new ArrayList<TimepointPredictionRecord>();

        TripDetailsQueryBean tdqb = new TripDetailsQueryBean();
        tdqb.setTripId(tripStatus.getActiveTrip().getId());
        tdqb.setServiceDate(tripStatus.getServiceDate());
        tdqb.setTime(new Date().getTime());
        tdqb.setInclusion(new TripDetailsInclusionBean(true, true, true));
        TripDetailsBean trip = _tripDetailsBeanService.getTripForId(tdqb);

        double scheduleDeviation = tripStatus.getScheduleDeviation();

        for (TripStopTimeBean stb : trip.getSchedule().getStopTimes()) {
            TimepointPredictionRecord tpr = new TimepointPredictionRecord();
            tpr.setTimepointId(AgencyAndIdLibrary.convertFromString(stb.getStop().getId()));
            tpr.setTimepointScheduledTime(trip.getStatus().getServiceDate() + (stb.getArrivalTime() * 1000));
            tpr.setTimepointPredictedTime(trip.getStatus().getServiceDate() + (int) (((double) stb.getArrivalTime() + scheduleDeviation) * 1000));
            tprs.add(tpr);
        }

        return tprs;
    }
}
