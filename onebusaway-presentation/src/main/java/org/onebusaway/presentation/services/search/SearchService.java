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
package org.onebusaway.presentation.services.search;

import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.presentation.model.SearchResultCollection;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.RouteBean;

import java.util.Set;

public interface SearchService {

  public SearchResultCollection getSearchResults(String query, SearchResultFactory resultFactory);

  public SearchResultCollection getSearchResultsForServiceDate(String query, SearchResultFactory resultFactory, ServiceDate serviceDate);

  public SearchResultCollection findRoutesStoppingNearPoint(Double latitude, Double longitude, SearchResultFactory resultFactory);

  public SearchResultCollection findRoutesStoppingWithinRegion(CoordinateBounds bounds, SearchResultFactory resultFactory);

  public SearchResultCollection findStopsNearPoint(Double latitude, Double longitude, SearchResultFactory resultFactory, Set<RouteBean> routeFilter);

}