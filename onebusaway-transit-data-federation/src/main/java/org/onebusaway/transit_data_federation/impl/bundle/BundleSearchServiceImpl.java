/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.transit_data_federation.impl.bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.bundle.BundleSearchService;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Proposes suggestions to the user based on bundle content--e.g. stop ID and route short names.
 * 
 * @author asutula
 *
 */
@Component
public class BundleSearchServiceImpl implements BundleSearchService {

	@Autowired
	private TransitDataService _transitDataService = null;

	private Map<String,List<String>> suggestions = Collections.synchronizedMap(new HashMap<String, List<String>>());

	@PostConstruct
	@Refreshable(dependsOn = RefreshableResources.TRANSIT_GRAPH)
	public void init() {
		Runnable initThread = new Runnable() {
			@Override
			public void run() {
				suggestions.clear();

				Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
				for (String agency : agencies.keySet()) {
					ListBean<RouteBean> routes = _transitDataService.getRoutesForAgencyId(agency);
					for (RouteBean route : routes.getList()) {
						String shortName = route.getShortName();
                                                if (shortName != null) 
                                                	generateInputsForString(shortName, "\\s+");
					}

					ListBean<String> stopIds = _transitDataService.getStopIdsForAgencyId(agency);
					for (String stopId : stopIds.getList()) {
						AgencyAndId agencyAndId = AgencyAndIdLibrary.convertFromString(stopId);
						generateInputsForString(agencyAndId.getId(), null);
					}
				}
			}
		};

		new Thread(initThread).start();
	}

	private void generateInputsForString(String string, String splitRegex) {
		String[] parts;
		if (splitRegex != null)
			parts = string.split(splitRegex);
		else
			parts = new String[] {string};
		for (String part : parts) {
			int length = part.length();
			for (int i = 0; i < length; i++) {
				String key = part.substring(0, i+1).toLowerCase();
				List<String> suggestion = suggestions.get(key);
				if (suggestion == null) {
					suggestion = new ArrayList<String>();
				}
				suggestion.add(string);
				Collections.sort(suggestion);
				suggestions.put(key, suggestion);
			}
		}
	}

	@Override
	public List<String> getSuggestions(String input) {
		List<String> suggestions = this.suggestions.get(input);
		if (suggestions == null)
			suggestions = new ArrayList<String>();
		if (suggestions.size() > 10)
			suggestions = suggestions.subList(0, 10);
		return suggestions;
	}
}