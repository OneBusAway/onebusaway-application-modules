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
package org.onebusaway.webapp.gwt.oba_application.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.MinTravelTimeToStopsBean;
import org.onebusaway.transit_data.model.oba.TimedPlaceBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.webapp.gwt.common.model.ModelEventSink;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchCompleteState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchProgressState;
import org.onebusaway.webapp.gwt.oba_application.model.ResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;
import org.onebusaway.webapp.gwt.oba_application.search.LocalSearchCallback;
import org.onebusaway.webapp.gwt.oba_application.search.LocalSearchProvider;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class LocalSearchHandler implements LocalSearchCallback {

  private Map<String, LocalSearchResult> _placeResults = new HashMap<String, LocalSearchResult>();

  private PlaceHandler _placeHandler = new PlaceHandler();

  private LocalSearchProvider _searchProvider;

  private ModelEventSink<StateEvent> _events;

  private ResultsModel _model;

  private TransitShedConstraintsBean _constraints;

  private MinTravelTimeToStopsBean _travelTimes;

  private List<CoordinateBounds> _searchGrid;

  private List<String> _queries = new ArrayList<String>();

  private List<String> _categories = new ArrayList<String>();

  private int _queryIndex = 0;

  private int _gridIndex = 0;

  private int _searchCount = 0;

  private boolean _complete = false;

  private String _resultId;

  public LocalSearchHandler(TransitShedConstraintsBean constraints,
      MinTransitTimeResult result) {
    _constraints = constraints;
    _travelTimes = result.getMinTravelTimeToStops();
    _searchGrid = result.getSearchGrid();
    _resultId = Long.toString(System.currentTimeMillis());
    for (CoordinateBounds bounds : _searchGrid)
      System.out.println(bounds);
  }

  public void setEventSink(ModelEventSink<StateEvent> events) {
    _events = events;
  }

  public void setLocalSearchProvider(LocalSearchProvider searchProvider) {
    _searchProvider = searchProvider;
  }

  public void setModel(ResultsModel model) {
    _model = model;
  }

  public void addQuery(String query, String category) {
    _queries.add(query);
    _categories.add(category);

  }

  public void run() {
    for (int i = 0; i < 2; i++)
      nextGrid();
  }

  public void onSuccess(List<LocalSearchResult> results) {

    if (results.isEmpty()) {

      _searchCount++;
      nextGrid();

    } else {

      for (LocalSearchResult result : results)
        _placeResults.put(result.getId(), result);

      WebappServiceAsync service = WebappServiceAsync.SERVICE;
      service.getLocalPathsToStops(_constraints.getConstraints(), _travelTimes,
          results, _placeHandler);
    }
  }

  public void onFailure(Throwable ex) {
    ex.printStackTrace();
  }

  private void nextGrid() {

    if (_gridIndex == _searchGrid.size()) {
      _queryIndex++;
      _gridIndex = 0;
    }

    if (checkCompletion())
      return;

    CoordinateBounds bounds = _searchGrid.get(_gridIndex++);
    String query = _queries.get(_queryIndex);
    String category = _categories.get(_queryIndex);
    _searchProvider.search(bounds, query, category, this);
  }

  private boolean checkCompletion() {

    double total = _searchGrid.size() * _queries.size();

    if (_gridIndex < _searchGrid.size() && _queryIndex < _queries.size()) {
      double processed = _queryIndex * _searchGrid.size() + _gridIndex;
      _events.fireModelChange(new StateEvent(new SearchProgressState(processed
          / total)));
      return false;
    }

    if (!_complete && _searchCount == total) {
      _complete = true;
      _events.fireModelChange(new StateEvent(new SearchCompleteState()));
    }

    return true;
  }

  private class PlaceHandler implements AsyncCallback<List<TimedPlaceBean>> {

    public void onSuccess(List<TimedPlaceBean> beans) {

      List<TimedLocalSearchResult> results = new ArrayList<TimedLocalSearchResult>(
          beans.size());

      for (final TimedPlaceBean bean : beans) {
        final LocalSearchResult result = _placeResults.get(bean.getPlaceId());
        TimedLocalSearchResult r = new TimedLocalSearchResult(_resultId,
            result, bean);
        results.add(r);
      }

      _model.addEntries(results);
      _searchCount++;
      nextGrid();
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
      _searchCount++;
      nextGrid();
    }
  }

}
