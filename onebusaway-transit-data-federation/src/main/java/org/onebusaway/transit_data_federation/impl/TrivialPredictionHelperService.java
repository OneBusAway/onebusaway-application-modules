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
package org.onebusaway.transit_data_federation.impl;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.TransitDataConstants;
import org.onebusaway.transit_data.model.trips.TimepointPredictionBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.util.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.PredictionHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Trivial implementation of the PredictionHelperService.  It returns a single prediction
 * for the tripStatus, or prediction records if available.
 *
 */
@Component
public class TrivialPredictionHelperService implements PredictionHelperService {

	private static Logger _log = LoggerFactory.getLogger(TrivialPredictionHelperService.class);
	
	@Autowired
	private TransitDataService _transitDataService;

	@Override
	public List<TimepointPredictionRecord> getPredictionRecordsForTrip(String agencyId,
			TripStatusBean tripStatus) {
		List<TimepointPredictionRecord> records = null;

		if (agencyId == null)
			return records;
		if (tripStatus == null)
			return records;
		if (!tripStatus.isPredicted())
			return records;
		// don't return predictions for a canceled trip
		if (TransitDataConstants.STATUS_CANCELED.equals(tripStatus.getStatus()))
			return records;

		records = new ArrayList<TimepointPredictionRecord>();

		List<TimepointPredictionBean> beans = tripStatus.getTimepointPredictions();

		if (beans != null && beans.size() > 0) {
			for (TimepointPredictionBean bean : beans) {
				TimepointPredictionRecord tpr = new TimepointPredictionRecord();

				tpr.setTimepointId(AgencyAndIdLibrary.convertFromString(bean.getTimepointId()));
				tpr.setTimepointScheduledTime(bean.getTimepointScheduledTime());
				tpr.setTimepointPredictedArrivalTime(bean.getTimepointPredictedArrivalTime());
				tpr.setTimepointPredictedDepartureTime(bean.getTimepointPredictedDepartureTime());
				tpr.setStopSequence(bean.getStopSequence());
				tpr.setTripId(AgencyAndIdLibrary.convertFromString(bean.getTripId()));
					tpr.setScheduleRealtionship(bean.getScheduleRelationship().getValue());

				records.add(tpr);
			}

			return records;
		}

		TimepointPredictionRecord tpr = new TimepointPredictionRecord();
		tpr.setTimepointId(AgencyAndIdLibrary.convertFromString(tripStatus.getNextStop().getId()));
		tpr.setTimepointScheduledTime(tripStatus.getLastUpdateTime() + tripStatus.getNextStopTimeOffset() * 1000);
		tpr.setTimepointPredictedArrivalTime((long) (tpr.getTimepointScheduledTime() + tripStatus.getScheduleDeviation()));
		tpr.setTimepointPredictedDepartureTime((long) (tpr.getTimepointScheduledTime() + tripStatus.getScheduleDeviation()));
		if (tpr.getScheduleRelationship() != null) {
			tpr.setScheduleRealtionship(tpr.getScheduleRelationship().getValue());
		} else {
			_log.info("no schedule relationship for trip " + tripStatus.getActiveTrip().getId());
			tpr.setScheduleRealtionship(TimepointPredictionRecord.ScheduleRelationship.SKIPPED.getValue());
		}

		
		records.add(tpr);
		return records;
	}

}
