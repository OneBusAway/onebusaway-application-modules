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
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.impl.beans.RouteBeanIdComparator;
import org.onebusaway.transit_data_federation.impl.beans.StopBeanIdComparator;
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

	private static final int MAX_TYPE_AHEAD_LENGTH = 32;
	@Autowired
	private TransitDataService _transitDataService = null;

	private Map<String,List<String>> suggestions = Collections.synchronizedMap(new HashMap<String, List<String>>());
	private Map<String,List<StopBean>> stopSuggestions = Collections.synchronizedMap(new HashMap<>());
	private Map<String,List<RouteBean>> routeSuggestions = Collections.synchronizedMap(new HashMap<>());

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
				SearchState searchState = createSearchState();

				Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
				for (String agency : agencies.keySet()) {
					ListBean<RouteBean> routes = _transitDataService.getRoutesForAgencyId(agency);
					for (RouteBean route : routes.getList()) {
						String shortName = route.getShortName();
						String hint = route.getLongName();
						if (hint == null) hint = route.getId(); // don't let hint be null
						generateInputsForString(createRouteSuggestionState(searchState, shortName, hint, route));
					}

					ListBean<String> stopIds = _transitDataService.getStopIdsForAgencyId(agency);
					for (String stopId : stopIds.getList()) {
						if (_transitDataService.stopHasRevenueService(agency, stopId)) {
							AgencyAndId agencyAndId = AgencyAndIdLibrary.convertFromString(stopId);
							StopBean stop = _transitDataService.getStop(stopId);
							String hint = null;
							if (stop != null) {
								hint = stop.getName();
							}
							// this is unlikley, but prevent hint from being null
							if (hint == null) {
								hint = stop.getId();
							}
							generateInputsForString(createStopSuggestionState(searchState, agencyAndId.getId(), hint, stop));
						}
					}
				}
				suggestions = searchState.getSuggestions();
				stopSuggestions = searchState.getStopSuggestions();
				routeSuggestions = searchState.getRouteSuggestions();
				_log.info("complete with suggestions {}, stopSuggestions {}, and routeSuggestions {}",
								suggestions.size(), stopSuggestions.size(), routeSuggestions.size());
			}
		};

		new Thread(initThread).start();
	}

	private SearchState createSearchState() {
		SearchState state = new SearchState();
		return state;
	}

	private SuggestionState createStopSuggestionState(SearchState searchState, String name, String hint, StopBean stop) {
		SuggestionState state = new SuggestionState(searchState, name, hint);
		state.setStop(stop);
		return state;
	}

	private SuggestionState createRouteSuggestionState(SearchState searchState, String name, String hint, RouteBean route) {
		SuggestionState state = new SuggestionState(searchState, name, hint);
		state.setRoute(route);
		return state;
	}

	private void generateInputsForString(SuggestionState state) {
		String string = state.getString();
		String splitRegex = state.getRegex();
		String hint = state.getHint();

		List<String> generalSearchParts = splitParts(splitRegex, string);

		for (String searchPart : generalSearchParts) {
			state.addSuggestion(searchPart, string + " [" + hint + "]");
		}

		if (state.getRoute() != null) {
			List<String> routeSearchParts = splitParts(splitRegex, hint);
			for (String routeSearchPart : routeSearchParts) {
				state.addRouteSuggestion(routeSearchPart, state.getRoute());
			}
		}

		if (state.getStop() != null) {
			List<String> stopSearchParts = splitParts(splitRegex, hint);
			for (String stopSearchPart : stopSearchParts) {
				state.addStopSuggestion(stopSearchPart, state.getStop());
			}
		}

	}

	// package private for unit tests
	 List<String> splitParts(String splitRegex, String string) {
		String[] parts;
		ArrayList<String> results = new ArrayList<>();
		if (string == null) return results;
		if (splitRegex != null)
			parts = string.split(splitRegex);
		else
			parts = new String[] {string};
		for (String part : parts) {
			int length = part.length();
			for (int i = 0; i < length; i++) {
				// here we add keys comprised of all the possible typeaheads for the first term (part)
				String key = part.substring(0, i+1).toLowerCase();
				results.add(key);
			}
		}
		if (parts.length > 1) {
			// we have more than one term (part)
			// now add keys comprised of the successive word typeaheads up to MAX_TYPE_AHEAD_LENGTH
			// this allows auto complete to work for multi-word searches
			int startPos = parts[0].length();
			for (int i = startPos; i < Math.min(string.length(), MAX_TYPE_AHEAD_LENGTH); i++) {
				String key = string.substring(0, i+1).toLowerCase();
				results.add(key);
			}

			if (string.length() > MAX_TYPE_AHEAD_LENGTH) {
				// add in the entire search term as well
				results.add(string.toLowerCase());
			}
		}
		return results;
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

	public ListBean<StopBean> getStopSuggestions(String input, int maxCount) {
		List<StopBean> tmpStopSuggestions = this.stopSuggestions.get(input);
		boolean limitExceeded = false;
		if (tmpStopSuggestions == null)
			tmpStopSuggestions = new ArrayList<>();
		if (tmpStopSuggestions.size() > maxCount) {
			limitExceeded = true;
			tmpStopSuggestions = tmpStopSuggestions.subList(0, maxCount);
		}
		return new ListBean<StopBean>(tmpStopSuggestions, limitExceeded);
	}

	public ListBean<RouteBean> getRouteSuggestions(String input, int maxCount) {
		List<RouteBean> tmpRouteSuggestions = this.routeSuggestions.get(input);
		boolean limitExceeded = false;
		if (tmpRouteSuggestions == null)
			tmpRouteSuggestions = new ArrayList<>();
		if (tmpRouteSuggestions.size() > maxCount) {
			limitExceeded = true;
			tmpRouteSuggestions = tmpRouteSuggestions.subList(0, maxCount);
		}
		return new ListBean<RouteBean>(tmpRouteSuggestions, limitExceeded);
	}

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (event instanceof ContextRefreshedEvent) {
      _initialized = true;
    }
  }

	private class SuggestionState {
		private SearchState searchState;
		private String name;
		private String hint;
		private String regex = "\\s+|-|/|\\(|\\)|&";
		private RouteBean route;
		private StopBean stop;


		public SuggestionState(SearchState searchState, String name, String hint) {
			this.searchState = searchState;
			this.name = name;
			this.hint = hint;
		}

		public String getString() {
			return name;
		}

		public String getRegex() {
			return regex;
		}

		public Map<String, List<String>> getSuggestions() {
			return searchState.getSuggestions();
		}

		public String getHint() {
			return hint;
		}
		public void setRoute(RouteBean route) {
			this.route = route;
		}
		public RouteBean getRoute() {
			return route;
		}
		public void setStop(StopBean stop) {
			this.stop = stop;
		}
		public StopBean getStop() {
			return stop;
		}

		public void addSuggestion(String key, String suggestion) {
			searchState.addSuggestion(key, suggestion);
		}

		public void addStopSuggestion(String key, StopBean stop) {
			searchState.addStopSuggestions(key, stop);
		}

		public void addRouteSuggestion(String key, RouteBean route) {
			searchState.addRouteSuggestions(key, route);
		}
	}

	private class SearchState {
		private Map<String,List<String>> stateSuggestions = Collections.synchronizedMap(new HashMap<String, List<String>>());
		private Map<String,List<StopBean>> stopSuggestions = Collections.synchronizedMap(new HashMap<>());
		private Map<String,List<RouteBean>> routeSuggestions = Collections.synchronizedMap(new HashMap<>());
		public SearchState() {

		}
		public Map<String,List<String>> getSuggestions() {
			return stateSuggestions;
		}
		public Map<String,List<StopBean>> getStopSuggestions() {
			return stopSuggestions;
		}
		public Map<String,List<RouteBean>> getRouteSuggestions() {
			return routeSuggestions;
		}

		public void addSuggestion(String key, String value) {
			List<String> suggestion = getSuggestions().get(key);
			if (suggestion == null) {
				suggestion = new ArrayList<String>();
			}
			suggestion.add(value);
			Collections.sort(suggestion);
			stateSuggestions.put(key, suggestion);
		}

		public void addRouteSuggestions(String key, RouteBean route) {
			List<RouteBean> suggestion = getRouteSuggestions().get(key);
			if (suggestion == null) {
				suggestion = new ArrayList<>();
			}
			suggestion.add(route);
			Collections.sort(suggestion, new RouteBeanIdComparator());
			routeSuggestions.put(key, suggestion);
		}

		public void addStopSuggestions(String key, StopBean stop) {
			List<StopBean> suggestion = getStopSuggestions().get(key);
			if (suggestion == null) {
				suggestion = new ArrayList<>();
			}
			suggestion.add(stop);
			Collections.sort(suggestion, new StopBeanIdComparator());
			stopSuggestions.put(key, suggestion);
		}
	}
}
