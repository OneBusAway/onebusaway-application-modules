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

import java.util.List;

import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.onebusaway.transit_data_federation.services.PredictionHelperService;
import org.springframework.stereotype.Component;

/**
 * Trivial implementation of the PredictionHelperService.  That is, it does nothing.
 *
 */
@Component
public class TrivialPredictionHelperService implements PredictionHelperService {

	@Override
	public List<TimepointPredictionRecord> getPredictionRecordsForTrip(String agencyId,
			TripStatusBean tripStatus) {
		return null;
	}

}
