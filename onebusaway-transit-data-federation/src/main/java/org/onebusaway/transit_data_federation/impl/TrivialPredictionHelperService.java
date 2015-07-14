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
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.onebusaway.transit_data_federation.services.PredictionHelperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Trivial implementation of the PredictionHelperService.  It returns a single prediction
 * for the tripStatus.
 *
 */
@Component
public class TrivialPredictionHelperService implements PredictionHelperService {

	
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
		
		records = new ArrayList<TimepointPredictionRecord>();
		TimepointPredictionRecord tpr = new TimepointPredictionRecord();
		tpr.setTimepointId(AgencyAndIdLibrary.convertFromString(tripStatus.getNextStop().getId()));
		tpr.setTimepointScheduledTime(tripStatus.getLastUpdateTime() + tripStatus.getNextStopTimeOffset() * 1000);
		tpr.setTimepointPredictedTime((long) (tpr.getTimepointScheduledTime() + tripStatus.getScheduleDeviation()));
		
		
		records.add(tpr);
		return records;
	}

}
