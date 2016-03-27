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
package org.onebusaway.presentation.impl.search;

import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderResult;
import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderService;
import org.onebusaway.presentation.impl.RouteComparator;
import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.presentation.model.SearchResultCollection;
import org.onebusaway.presentation.services.search.SearchResultFactory;
import org.onebusaway.presentation.services.search.SearchService;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.exceptions.NoSuchStopServiceException;
import org.onebusaway.exceptions.OutOfServiceAreaServiceException;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.RoutesBean;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that constructs search results given a search result factory that is
 * inferface specific. We need to create interface-specific models to pass into
 * the JSP for generation of HTML.
 * 
 * @author jmaki
 *
 */
@Component
public class SearchServiceImpl implements SearchService {

	// the pattern of what can be leftover after prefix/suffix matching for a
	// route to be a "suggestion" for a given search
	private static final Pattern leftOverMatchPattern = Pattern
			.compile("^([A-Z]|-)+$");

	// when querying for routes from a lat/lng, use this distance in meters
	private static final double DISTANCE_TO_ROUTES = 600;

	// The max number of closest routes to display
	private static final int MAX_ROUTES = 10;

	// when querying for stops from a lat/lng, use this distance in meters
	private static final int DISTANCE_TO_STOPS = 600;

	// The max number of closest stops to display
	private static final int MAX_STOPS = 10;

	@Autowired
	private EnterpriseGeocoderService _geocoderService;

	@Autowired
	private TransitDataService _transitDataService;

	private Map<String, RouteBean> _routeShortNameToRouteBeanMap = new HashMap<String, RouteBean>();
	
	private Map<String, RouteBean> _routeIdToRouteBeanMap = new HashMap<String, RouteBean>();

	private Map<String, RouteBean> _routeLongNameToRouteBeanMap = new HashMap<String, RouteBean>();

	private Map<String, String> _stopCodeToStopIdMap = new HashMap<String, String>();

	private String _bundleIdForCaches = null;

	// we keep an internal cache of route short/long names because if we moved
	// this into the
	// transit data federation, we'd also have to move the model factory and
	// some other agency-specific
	// conventions, which wouldn't be pretty.
	//
	// long-term FIXME: figure out how to split apart the model creation a bit
	// more from the actual
	// search process.
	public void refreshCachesIfNecessary() {
		String currentBundleId = _transitDataService.getActiveBundleId();

		if ((_bundleIdForCaches != null && _bundleIdForCaches
				.equals(currentBundleId)) || currentBundleId == null) {
			return;
		}

		_routeShortNameToRouteBeanMap.clear();
		_routeIdToRouteBeanMap.clear();
		_routeLongNameToRouteBeanMap.clear();
		_stopCodeToStopIdMap.clear();

		for (AgencyWithCoverageBean agency : _transitDataService
				.getAgenciesWithCoverage()) {
			for (RouteBean routeBean : _transitDataService
					.getRoutesForAgencyId(agency.getAgency().getId()).getList()) {
				if (routeBean.getShortName() != null)
					_routeShortNameToRouteBeanMap.put(routeBean.getShortName()
							.toUpperCase(), routeBean);
				if (routeBean.getLongName() != null)
					_routeLongNameToRouteBeanMap.put(routeBean.getLongName(),
							routeBean);
				
				_routeIdToRouteBeanMap.put(routeBean.getId(), routeBean);
			}

			SearchQueryBean query = new SearchQueryBean();
			query.setBounds(getAgencyBounds(agency));
			query.setMaxCount(Integer.MAX_VALUE);

			StopsBean stops = _transitDataService.getStops(query);
			List<StopBean> stopsList = stops.getStops();

			for (StopBean stop : stopsList) {
				_stopCodeToStopIdMap.put(agency.getAgency().getId() + "_"
						+ stop.getCode().toUpperCase(), stop.getId());
			}
		}

		_bundleIdForCaches = currentBundleId;
	}

	private CoordinateBounds getAgencyBounds(AgencyWithCoverageBean agency) {
		CoordinateBounds bounds = new CoordinateBounds();

		double lat = agency.getLat();
		double lon = agency.getLon();
		double latSpan = agency.getLatSpan() / 2;
		double lonSpan = agency.getLonSpan() / 2;
		bounds.addPoint(lat - latSpan, lon - lonSpan);
		bounds.addPoint(lat + latSpan, lon + lonSpan);

		return bounds;
	}

	@Override
	public SearchResultCollection findStopsNearPoint(Double latitude,
			Double longitude, SearchResultFactory resultFactory,
			Set<RouteBean> routeFilter) {

		CoordinateBounds bounds = SphericalGeometryLibrary.bounds(latitude,
				longitude, DISTANCE_TO_STOPS);

		SearchQueryBean queryBean = new SearchQueryBean();
		queryBean.setType(SearchQueryBean.EQueryType.BOUNDS_OR_CLOSEST);
		queryBean.setBounds(bounds);
		queryBean.setMaxCount(100);

		StopsBean stops = _transitDataService.getStops(queryBean);

		Collections.sort(stops.getStops(), new StopDistanceFromPointComparator(
				latitude, longitude));

		// A list of stops that will go in our search results
		List<StopBean> stopsForResults = new ArrayList<StopBean>();

		// Keep track of which routes are already in our search results by
		// direction
		Map<String, List<RouteBean>> routesByDirectionAlreadyInResults = new HashMap<String, List<RouteBean>>();

		// Cache stops by route so we don't need to call the transit data
		// service repeatedly for the same route
		Map<String, StopsForRouteBean> stopsForRouteLookup = new HashMap<String, StopsForRouteBean>();

		// Iterate through each stop and see if it adds additional routes for a
		// direction to our final results.
		for (StopBean stopBean : stops.getStops()) {

			// Get the stop bean that is actually inside this search result. We
			// kept track of it earlier.
			// StopBean stopBean = stopBeanBySearchResult.get(stopResult);

			// Record of routes by direction id for this stop
			Map<String, List<RouteBean>> routesByDirection = new HashMap<String, List<RouteBean>>();

			for (RouteBean route : stopBean.getRoutes()) {
				// route is a route serving the current stopBeanForSearchResult

				// Query for all stops on this route
				StopsForRouteBean stopsForRoute = stopsForRouteLookup.get(route
						.getId());
				if (stopsForRoute == null) {
					stopsForRoute = _transitDataService.getStopsForRoute(route
							.getId());
					stopsForRouteLookup.put(route.getId(), stopsForRoute);
				}

				// Get the groups of stops on this route. The id of each group
				// corresponds to a GTFS direction id for this route.
				for (StopGroupingBean stopGrouping : stopsForRoute
						.getStopGroupings()) {
					for (StopGroupBean stopGroup : stopGrouping.getStopGroups()) {

						String directionId = stopGroup.getId();

						// Check if the current stop is served in this
						// direction. If so, record it.
						if (stopGroup.getStopIds().contains(stopBean.getId())) {
							if (!routesByDirection.containsKey(directionId)) {
								routesByDirection.put(directionId,
										new ArrayList<RouteBean>());
							}
							routesByDirection.get(directionId).add(route);
						}
					}
				}
			}

			// Iterate over routes binned by direction for this stop and compare
			// to routes by direction already in our search results
			boolean shouldAddStopToResults = false;
			for (Map.Entry<String, List<RouteBean>> entry : routesByDirection
					.entrySet()) {
				String directionId = entry.getKey();
				List<RouteBean> routesForThisDirection = entry.getValue();

				if (!routesByDirectionAlreadyInResults.containsKey(directionId)) {
					routesByDirectionAlreadyInResults.put(directionId,
							new ArrayList<RouteBean>());
				}

				@SuppressWarnings("unchecked")
				List<RouteBean> additionalRoutes = ListUtils.subtract(
						routesForThisDirection,
						routesByDirectionAlreadyInResults.get(directionId));
				if (additionalRoutes.size() > 0) {
					// This stop is contributing new routes in this direction,
					// so add these additional
					// stops to our record of stops by direction already in
					// search results and toggle
					// flag that tells to to add the stop to the search results.
					routesByDirectionAlreadyInResults.get(directionId).addAll(
							additionalRoutes);
					// We use this flag because we want to add new routes to our
					// record potentially for each
					// direction id, but we only want to add the stop to the
					// search results once. It happens below.
					shouldAddStopToResults = true;
				}
			}
			if (shouldAddStopToResults) {
				// Add the stop to our search results
				stopsForResults.add(stopBean);
			}
			// Break out of iterating through stops if we've reached our max
			if (stopsForResults.size() >= MAX_STOPS) {
				break;
			}
		}

		// Create our search results object, iterate through our stops, create
		// stop
		// results from each of those stops, and add them to the search results.
		SearchResultCollection results = new SearchResultCollection();
		results.addRouteFilters(routeFilter);

		for (StopBean stop : stopsForResults) {
			SearchResult result = resultFactory
					.getStopResult(stop, routeFilter);
			results.addMatch(result);
		}

		return results;
	}

	@Override
	public SearchResultCollection findRoutesStoppingWithinRegion(
			CoordinateBounds bounds, SearchResultFactory resultFactory) {
	  SearchResultCollection results = new SearchResultCollection();
		SearchQueryBean queryBean = new SearchQueryBean();
		queryBean.setType(SearchQueryBean.EQueryType.BOUNDS_OR_CLOSEST);
		queryBean.setBounds(bounds);
		queryBean.setMaxCount(100);

		RoutesBean routes = null;
		try {
		  routes = _transitDataService.getRoutes(queryBean);
		} catch (OutOfServiceAreaServiceException e) {
		  return results;
		}
		
		Collections.sort(routes.getRoutes(), new RouteComparator());

		for (RouteBean route : routes.getRoutes()) {
			results.addMatch(resultFactory.getRouteResultForRegion(route));
		}

		return results;
	}

	@Override
	public SearchResultCollection findRoutesStoppingNearPoint(Double latitude,
			Double longitude, SearchResultFactory resultFactory) {
		CoordinateBounds bounds = SphericalGeometryLibrary.bounds(latitude,
				longitude, DISTANCE_TO_ROUTES);

		SearchResultCollection results = new SearchResultCollection();
		SearchQueryBean queryBean = new SearchQueryBean();
		queryBean.setType(SearchQueryBean.EQueryType.BOUNDS_OR_CLOSEST);
		queryBean.setBounds(bounds);
		queryBean.setMaxCount(100);

		
		RoutesBean routes = null;
		try {
		  routes = _transitDataService.getRoutes(queryBean);
    } catch (OutOfServiceAreaServiceException e) {
      return results;
    }

		Collections.sort(routes.getRoutes(),
				new RouteDistanceFromPointComparator(latitude, longitude));


		for (RouteBean route : routes.getRoutes()) {

			SearchResult result = resultFactory.getRouteResult(route);

			results.addMatch(result);

			if (results.getMatches().size() > MAX_ROUTES) {
				break;
			}
		}

		return results;
	}

	@Override
	public SearchResultCollection getSearchResults(String query,
			SearchResultFactory resultFactory) {
		refreshCachesIfNecessary();

		SearchResultCollection results = new SearchResultCollection();

		String normalizedQuery = normalizeQuery(results, query);

		tryAsRoute(results, normalizedQuery, resultFactory);

		// only guess it as a stop if its numeric or has possible agency prefix
		// results does not support mixed types -- it can only be a route or a stop
		if (results.isEmpty() && (StringUtils.isNumeric(normalizedQuery) || normalizedQuery.contains("_")) ) {
			tryAsStop(results, normalizedQuery, resultFactory);
		}

		if (results.isEmpty()) {
			tryAsGeocode(results, normalizedQuery, resultFactory);
		}

		return results;
	}

	private String normalizeQuery(SearchResultCollection results, String q) {
		if (q == null) {
			return null;
		}

		q = URLDecoder.decode(q);
		
		q = q.trim();

		List<String> tokens = new ArrayList<String>();
		if (Boolean.TRUE.equals(configuredAgencyIdHasSpaces())) {
  		String agencyMatch = matchAgencyIds(q);
  		if (agencyMatch != null) {
  		  // handle agencies with spaces
  		  q = q.replace(agencyMatch, "");
  		  tokens.add(agencyMatch);
  		}
		}
		
		StringTokenizer tokenizer = new StringTokenizer(q, " +", true);
		if (configuredAgencyIdHasSpaces() && !tokens.isEmpty() && tokenizer.hasMoreTokens()) {
		  String peek = tokenizer.nextToken().trim();
		  if (peek.contains("_")) {
		    // we have an agency id + "_" that was probably meant for the last token
		    tokens.set(0,  tokens.get(0) + peek);
		  } else {
		    if (!StringUtils.isEmpty(peek)) {
		      tokens.add(peek);
		    }
		  }
		}
		
		while (tokenizer.hasMoreTokens()) {
		  /*
		   * Don't upper case the token -- as transit data service queries are
		   * case sensitive.  However, the maps cached in this class are all
		   * upper case for convenience.
		   */
			String token = tokenizer.nextToken().trim(); 

			if (!StringUtils.isEmpty(token)) {
				tokens.add(token);
			}
		}

		String normalizedQuery = "";
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			String lastItem = null;
			String nextItem = null;
			if (i - 1 >= 0) {
				lastItem = tokens.get(i - 1);
			}
			if (i + 1 < tokens.size()) {
				nextItem = tokens.get(i + 1);
			}

			// keep track of route tokens we found when parsing
			if (_routeShortNameToRouteBeanMap.containsKey(token.toUpperCase())) {
				// if a route is included as part of another type of query, then
				// it's a filter--
				// so remove it from the normalized query sent to the geocoder
				// or stop service
				if ((lastItem != null && !_routeShortNameToRouteBeanMap
						.containsKey(lastItem.toUpperCase()))
						|| (nextItem != null && !_routeShortNameToRouteBeanMap
								.containsKey(nextItem.toUpperCase()))) {
					results.addRouteFilter(_routeShortNameToRouteBeanMap
							.get(token.toUpperCase()));
					continue;
				}
			} else {
				// if the token is not a route and the next or last token is a
				// valid stop id (but not also a route ID),
				// consider the token a bad filter and remove it from the query.
				if ((lastItem != null && stopsForId(lastItem).size() > 0
						&& !_routeShortNameToRouteBeanMap.containsKey(lastItem.toUpperCase()))
						|| (nextItem != null && stopsForId(nextItem).size() > 0
								&& !_routeShortNameToRouteBeanMap.containsKey(nextItem.toUpperCase()))) {
					if (!token.contains("_")) {
						// if we have an agency Id, its probably a stop, don't
						// discard
						continue;
					}
				}
			}

			// allow the plus sign instead of "and"
			if (token.equals("+")) {
				// if a user is prepending a route filter with a plus sign, chop
				// it off
				// e.g. main and craig + B63
				if (_routeShortNameToRouteBeanMap.containsKey(nextItem.toUpperCase())) {
					continue;
				}

				token = "and";
			}

			normalizedQuery += token + " ";
		}

		// If we parsed more than one route filter from the query, remove route
		// filters
		// because user interfaces don't support more than one route filter
		if (results.getRouteFilter() != null
				&& results.getRouteFilter().size() > 1) {
			results.getRouteFilter().clear();
		}

		return normalizedQuery.trim();
	}

	private Boolean _agencyIdHasSpaces = null;
	private Boolean configuredAgencyIdHasSpaces() {
	  if (_agencyIdHasSpaces == null) {
	    Boolean agencyIdHasSpaces = Boolean.FALSE;
	    for (String id : getAgencyIds()) {
	      if (id.contains(" ")) {
	        agencyIdHasSpaces = Boolean.TRUE;
	        break;
	      }
	    }
	    // see if another thread beat us to it
	    if (_agencyIdHasSpaces == null) {
	      _agencyIdHasSpaces = agencyIdHasSpaces;
	    }
	  }
    return _agencyIdHasSpaces;
  }

  private String matchAgencyIds(String q) {
	  for (String id : getAgencyIds()) {
	    if (q.contains(id)) {
	      return id;
	    }
	  }
    return null;
  }


	private List<String> _agencyIds = null;
  private List<String> getAgencyIds() {
    if (_agencyIds == null) {
      List<String> agencyIds = new ArrayList<String>();
      for(AgencyWithCoverageBean agenciesWithCoverage : _transitDataService.getAgenciesWithCoverage()) {
        agencyIds.add(agenciesWithCoverage.getAgency().getId());
      }
      // see if another thread beat us to it
      if (_agencyIds == null) {
        _agencyIds = agencyIds;
      }
    }
    return _agencyIds;
  }

  private void tryAsRoute(SearchResultCollection results, String routeQueryMixedCase,
			SearchResultFactory resultFactory) {
	  
	  String routeQuery = new String(routeQueryMixedCase);
		if (routeQuery == null || StringUtils.isEmpty(routeQuery)) {
			return;
		}

		routeQuery = routeQuery.toUpperCase().trim();

		if (routeQuery.length() < 1) {
			return;
		}

		// agency + route id matching (from direct links)
    if (_routeIdToRouteBeanMap.get(routeQueryMixedCase) != null) {
      RouteBean routeBean = _routeIdToRouteBeanMap.get(routeQueryMixedCase);
      results.addMatch(resultFactory.getRouteResult(routeBean));
      // if we've matched, assume no others
      return;
    }

    // agency + route id matching (from direct links)
    if (_routeIdToRouteBeanMap.get(routeQuery) != null) {
      RouteBean routeBean = _routeIdToRouteBeanMap.get(routeQuery);
      results.addMatch(resultFactory.getRouteResult(routeBean));
      // if we've matched, assume no others
      return;
    }
		
		// short name matching
		if (_routeShortNameToRouteBeanMap.get(routeQuery) != null) {
			RouteBean routeBean = _routeShortNameToRouteBeanMap.get(routeQuery);
			results.addMatch(resultFactory.getRouteResult(routeBean));
		}

		for (String routeShortName : _routeShortNameToRouteBeanMap.keySet()) {
			// if the route short name ends or starts with our query, and
			// whatever's left over
			// matches the regex
			String leftOvers = routeShortName.replace(routeQuery, "");
			Matcher matcher = leftOverMatchPattern.matcher(leftOvers);
			Boolean leftOversAreDiscardable = matcher.find();

			if (!routeQuery.equals(routeShortName)
					&& ((routeShortName.startsWith(routeQuery) && leftOversAreDiscardable) || (routeShortName
							.endsWith(routeQuery) && leftOversAreDiscardable))) {
			  try {
  			  RouteBean routeBean = _routeShortNameToRouteBeanMap
  						.get(routeShortName);
  				results.addSuggestion(resultFactory.getRouteResult(routeBean));
  				continue;
			  } catch (OutOfServiceAreaServiceException oosase) {
			  }

			}
		}

		// long name matching
		for (String routeLongName : _routeLongNameToRouteBeanMap.keySet()) {
			if (routeLongName.contains(routeQuery + " ")
					|| routeLongName.contains(" " + routeQuery)) {
			  try {
  				RouteBean routeBean = _routeLongNameToRouteBeanMap
  						.get(routeLongName);
  				results.addSuggestion(resultFactory.getRouteResult(routeBean));
  				continue;
			  } catch (OutOfServiceAreaServiceException oosase) {
			  }
			}
		}

	}

	private void tryAsStop(SearchResultCollection results, String stopQuery,
			SearchResultFactory resultFactory) {
		if (stopQuery == null || StringUtils.isEmpty(stopQuery)) {
			return;
		}

		stopQuery = stopQuery.trim();

		// try to find a stop ID for all known agencies
		List<StopBean> matches = stopsForId(stopQuery);

		if (matches.size() == 1)
			results.addMatch(resultFactory.getStopResult(matches.get(0),
					results.getRouteFilter()));
		else {
			for (StopBean match : matches) {
				results.addSuggestion(resultFactory.getStopResult(match,
						results.getRouteFilter()));
			}
		}
	}

	private void tryAsGeocode(SearchResultCollection results, String query,
			SearchResultFactory resultFactory) {
		List<EnterpriseGeocoderResult> geocoderResults = _geocoderService
				.enterpriseGeocode(query);

		// guard against misconfiguration
		if (geocoderResults == null) return;
		
		for (EnterpriseGeocoderResult result : geocoderResults) {
			if (geocoderResults.size() == 1) {
				results.addMatch(resultFactory.getGeocoderResult(result,
						results.getRouteFilter()));
			} else {
				results.addSuggestion(resultFactory.getGeocoderResult(result,
						results.getRouteFilter()));
			}
		}
	}

	// Utility method for getting all known stops for an id with no agency
	private List<StopBean> stopsForId(String id) {
		List<StopBean> matches = new ArrayList<StopBean>();
		
		// accept agency denoted stops first!
		if (id.contains("_")) {
		  try {
		    StopBean potentialStop = _transitDataService.getStop(id);
		    if (potentialStop != null) {
		      matches.add(potentialStop);
		      // if an agency prefix was specified, don't continue searching
		      return matches;
		    }
		  } catch (NoSuchStopServiceException ex) {
		    
		  }
		}
		
		for (AgencyWithCoverageBean agency : _transitDataService
				.getAgenciesWithCoverage()) {
			AgencyAndId potentialStopId = new AgencyAndId(agency.getAgency()
					.getId(), id);

			try {
				StopBean potentialStop = _transitDataService
						.getStop(potentialStopId.toString());

				if (potentialStop != null) {
					matches.add(potentialStop);
				}
			} catch (NoSuchStopServiceException ex) {
				try {
				  if (matches.isEmpty()) {
				    // this search is faulty if multi-agency based
				    // skip if we have a match already
  					StopBean potentialStop = _transitDataService
  							.getStop(getStopIdFromStopCode(potentialStopId.toString()));
  					if (potentialStop != null) {
  						matches.add(potentialStop);
  					}
				  }
				} catch (NoSuchStopServiceException ex2) {
					continue;
				}
			}
		}
		return matches;
	}

	private String getStopIdFromStopCode(String code) {
		if (code != null
				&& _stopCodeToStopIdMap.containsKey(code.toUpperCase()))
			return _stopCodeToStopIdMap.get(code.toUpperCase());
		return code;
	}

	private class StopDistanceFromPointComparator implements
			Comparator<StopBean> {

		private double lat;
		private double lon;

		public StopDistanceFromPointComparator(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		@Override
		public int compare(StopBean o1, StopBean o2) {

			double d1 = SphericalGeometryLibrary.distanceFaster(this.lat,
					this.lon, o1.getLat(), o1.getLon());
			double d2 = SphericalGeometryLibrary.distanceFaster(this.lat,
					this.lon, o2.getLat(), o2.getLon());

			if (d1 < d2) {
				return -1;
			} else if (d1 > d2) {
				return +1;
			} else {
				return 0;
			}
		}
	}

	private class RouteDistanceFromPointComparator implements
			Comparator<RouteBean> {

		private double lat;
		private double lon;

		public RouteDistanceFromPointComparator(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		@Override
		public int compare(RouteBean o1, RouteBean o2) {

			Double d1 = getDistanceToNearestStopOnRoute(o1);
			Double d2 = getDistanceToNearestStopOnRoute(o2);

			if (d1 < d2) {
				return -1;
			} else if (d1 > d2) {
				return +1;
			} else {
				return 0;
			}
		}

		private Double getDistanceToNearestStopOnRoute(RouteBean route) {
			StopsForRouteBean stopsBean = _transitDataService
					.getStopsForRoute(route.getId());

			Double minDistanceToRoute = null;
			for (StopBean stop : stopsBean.getStops()) {
				Double distance = SphericalGeometryLibrary.distanceFaster(
						stop.getLat(), stop.getLon(), lat, lon);
				if (minDistanceToRoute == null) {
					minDistanceToRoute = distance;
					continue;
				}
				if (distance < minDistanceToRoute)
					minDistanceToRoute = distance;
			}

			return minDistanceToRoute;
		}
	}
}