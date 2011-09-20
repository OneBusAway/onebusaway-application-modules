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
/**
 * 
 */
package org.onebusaway.webapp.gwt.where_library.view.constraints;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.geospatial.services.SphericalGeometryLibrary;
import org.onebusaway.transit_data.model.SearchQueryBean;
import org.onebusaway.webapp.gwt.where_library.impl.CombinedSearchServiceImpl;
import org.onebusaway.webapp.gwt.where_library.services.CombinedSearchResult;
import org.onebusaway.webapp.gwt.where_library.services.CombinedSearchService;
import org.onebusaway.webapp.gwt.where_library.view.ResultsPanelManager;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderWidget;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class QueryOperationHandler implements OperationHandler,
    AsyncCallback<CombinedSearchResult> {

  private String _query;

  private LatLng _point;

  private OperationContext _context;

  public QueryOperationHandler(String query, LatLng point) {
    _query = query;
    _point = point;
  }

  @Override
  public void handleOperation(OperationContext context) {

    _context = context;

    StopFinderWidget widget = context.getWidget();
    widget.setSearchText(_query);

    LatLng center = _point;
    
    if( center == null) {
      MapWidget map = context.getMap();
      center = map.getCenter();
    }
    
    CoordinateBounds bounds = SphericalGeometryLibrary.bounds(
        center.getLatitude(), center.getLongitude(), 20000);

    SearchQueryBean query = new SearchQueryBean();
    query.setBounds(bounds);
    query.setMaxCount(10);
    query.setQuery(_query);

    CombinedSearchService search = new CombinedSearchServiceImpl();
    search.search(query, 5000, this);
  }

  /****
   * {@link AsyncCallback} Interface
   ****/

  @Override
  public void onSuccess(CombinedSearchResult result) {

    ResultsPanelManager manager = new ResultsPanelManager(_context);

    if (result.isEmpty()) {
      manager.addNoResultsMessage();
      return;
    }

    manager.setResults(result);
  }

  public void onFailure(Throwable ex) {

  }

  /****
   * {@link Object} Interface
   ****/

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof QueryOperationHandler))
      return false;

    QueryOperationHandler rc = (QueryOperationHandler) obj;
    return _query.equals(rc._query);
  }

  @Override
  public int hashCode() {
    return _query.hashCode();
  }

  /****
   * Private Methods
   ****/

}