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

import org.onebusaway.geocoder.enterprise.services.EnterpriseGeocoderResult;
import org.onebusaway.presentation.model.SearchResult;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;

import java.util.Set;

public interface SearchResultFactory {

  public SearchResult getRouteResult(RouteBean routeBean);

  public SearchResult getRouteResultForRegion(RouteBean routeBean);

  public SearchResult getStopResult(StopBean stopBean, Set<RouteBean> routeFilter);

  public SearchResult getGeocoderResult(EnterpriseGeocoderResult geocodeResult, Set<RouteBean> routeFilter);

}