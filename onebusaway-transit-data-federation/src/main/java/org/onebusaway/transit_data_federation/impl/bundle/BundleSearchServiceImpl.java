/**
 * Copyright (C) 2015 Cambridge Systematics
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
import org.onebusaway.util.AgencyAndIdLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Proposes suggestions to the user based on bundle content--e.g. stop ID and route short names.
 * 
 * @author asutula
 *
 */
@Component
public class BundleSearchServiceImpl implements BundleSearchService, ApplicationListener {

	@Autowired
	private TransitDataService _transitDataService = null;

	private Map<String,List<String>> suggestions = Collections.synchronizedMap(new HashMap<String, List<String>>());

	private boolean _initialized = false;
	
	private static Logger _log = LoggerFactory
      .getLogger(BundleSearchServiceImpl.class);
	
	@PostConstruct
	@Refreshable(dependsOn = { 
		      RefreshableResources.ROUTE_COLLECTIONS_DATA, 
		      RefreshableResources.TRANSIT_GRAPH })
	public void init() {
		Runnable initThread = new Runnable() {
			@Override
			public void run() {
			  while (!_initialized) {
			    try {
            Thread.sleep(1 * 1000);
          } catch (InterruptedException e) {
            return;
          }
			  }
			  _log.info("building cache");
				Map<String,List<String>> tmpSuggestions = Collections.synchronizedMap(new HashMap<String, List<String>>());
				

				Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
				for (String agency : agencies.keySet()) {
					ListBean<RouteBean> routes = _transitDataService.getRoutesForAgencyId(agency);
					for (RouteBean route : routes.getList()) {
						String shortName = route.getShortName();
						generateInputsForString(tmpSuggestions, shortName, "\\s+");
					}

					ListBean<String> stopIds = _transitDataService.getStopIdsForAgencyId(agency);
					for (String stopId : stopIds.getList()) {
						if (_transitDataService.stopHasRevenueService(agency, stopId)) {
							AgencyAndId agencyAndId = AgencyAndIdLibrary.convertFromString(stopId);
							generateInputsForString(tmpSuggestions, agencyAndId.getId(), null);
						}
					}
				}
				suggestions = tmpSuggestions;
				_log.info("complete");
			}
		};

		new Thread(initThread).start();
	}

	private void generateInputsForString(Map<String,List<String>> tmpSuggestions, String string, String splitRegex) {
		String[] parts;
		if (string == null) return;
		if (splitRegex != null)
			parts = string.split(splitRegex);
		else
			parts = new String[] {string};
		for (String part : parts) {
			int length = part.length();
			for (int i = 0; i < length; i++) {
				String key = part.substring(0, i+1).toLowerCase();
				List<String> suggestion = tmpSuggestions.get(key);
				if (suggestion == null) {
					suggestion = new ArrayList<String>();
				}
				suggestion.add(string);
				Collections.sort(suggestion);
				tmpSuggestions.put(key, suggestion);
			}
		}
	}

	@Override
	public List<String> getSuggestions(String input) {
		List<String> tmpSuggestions = this.suggestions.get(input);
		if (tmpSuggestions == null)
			tmpSuggestions = new ArrayList<String>();
		if (tmpSuggestions.size() > 10)
			tmpSuggestions = tmpSuggestions.subList(0, 10);
		return tmpSuggestions;
	}

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (event instanceof ContextRefreshedEvent) {
      _initialized = true;
    }
  }
}
