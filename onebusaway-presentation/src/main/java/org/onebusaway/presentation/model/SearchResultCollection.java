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
package org.onebusaway.presentation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.transit_data.model.RouteBean;

public class SearchResultCollection implements Serializable {

  private static final long serialVersionUID = 1L;

  private Class<? extends SearchResult> _resultType = null;
  
  private List<SearchResult> _matches = new ArrayList<SearchResult>();

  private List<SearchResult> _suggestions = new ArrayList<SearchResult>();

  private Set<RouteBean> _routeFilter = new HashSet<RouteBean>();
  
  private Double _queryLat = null;
  
  private Double _queryLon = null;

  private Boolean _isGeocode = false;

  private String _hint = "not set";

  public void addMatch(SearchResult thing) throws IllegalArgumentException {
    if(_resultType == null) { 
      _resultType = thing.getClass();
    }

    if(!_resultType.isInstance(thing)) {
      throw new IllegalArgumentException("All results must be of type " + _resultType);
    }
    
    _matches.add(thing);
  }

  public void addSuggestion(SearchResult thing) throws IllegalArgumentException {
    if(_resultType == null) { 
      _resultType = thing.getClass();
    }

    if(!_resultType.isInstance(thing)) {
      throw new IllegalArgumentException("All results must be of type " + _resultType);
    }
    
    _suggestions.add(thing);
  }

  public void addRouteFilter(RouteBean route) {
    _routeFilter.add(route);
  }
  
  public void addRouteFilters(Set<RouteBean> routes) {
    _routeFilter.addAll(routes);
  }

  public boolean isEmpty() {
    return ((_matches.size() + _suggestions.size()) == 0);
  }
  
  public List<SearchResult> getMatches() {
    return _matches;
  }

  public Set<RouteBean> getRouteFilter() {
    return _routeFilter;
  }

  public List<SearchResult> getSuggestions() {
    return _suggestions;
  }

  public String getResultType() {
    if(_resultType != null)
      return _resultType.getSimpleName();
    else
      return null;
  }

  public Double getQueryLat() {
    return _queryLat;
  }

  public void setQueryLat(Double queryLat) {
    this._queryLat = queryLat;
  }

  public Double getQueryLon() {
    return _queryLon;
  }

  public void setQueryLon(Double queryLon) {
    this._queryLon = queryLon;
  }

  public void setGeocode(Boolean isGeocode) { this._isGeocode = isGeocode; }

  public Boolean getIsGeocode() { return this._isGeocode; }

  public void setHint(String hint) { this._hint = hint; }

  public String getHint() { return _hint; }

  public String toString() {
    return "{matches=" + _matches 
        + ", suggestions=" + _suggestions
        + ", routeFilter=" + _routeFilter
        + "}";
  }

}