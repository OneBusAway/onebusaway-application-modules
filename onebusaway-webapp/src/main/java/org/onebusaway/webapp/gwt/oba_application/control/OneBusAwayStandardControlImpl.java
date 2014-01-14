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

import java.util.LinkedHashMap;
import java.util.Map;

import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchStartedState;
import org.onebusaway.webapp.gwt.oba_application.model.QueryModel;

import com.google.gwt.maps.client.geom.LatLng;

public class OneBusAwayStandardControlImpl extends CommonControlImpl implements
    OneBusAwayStandardControl {

  private QueryModel _queryModel;

  public void setQueryModel(QueryModel constraintsModel) {
    _queryModel = constraintsModel;
  }

  public void query(String query, String locationQuery, LatLng location,
      long time, TransitShedConstraintsBean constraints) {

    // We push the query onto the context so it can be bookmarked
    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put(ConstraintsParameterMapping.PARAM_QUERY, query);
    if (locationQuery != null && locationQuery.length() > 0)
      params.put(ConstraintsParameterMapping.PARAM_LOCATION, locationQuery);
    if (location != null) {
      params.put(ConstraintsParameterMapping.PARAM_LAT,
          Double.toString(location.getLatitude()));
      params.put(ConstraintsParameterMapping.PARAM_LON,
          Double.toString(location.getLongitude()));
    }

    ConstraintsParameterMapping.addTimeToParams(time, params);
    ConstraintsParameterMapping.addConstraintsToParams(constraints, params);

    params.put("_", Integer.toString(_contextIndex++));

    _contextHelper.setContext(params);
  }

  public void handleContext(Context context) {

    super.handleContext(context);

    if (context.hasParam(ConstraintsParameterMapping.PARAM_QUERY)) {

      String query = context.getParam(ConstraintsParameterMapping.PARAM_QUERY);

      String locationQuery = "";
      if (context.hasParam(ConstraintsParameterMapping.PARAM_LOCATION))
        locationQuery = context.getParam(ConstraintsParameterMapping.PARAM_LOCATION);

      LatLng location = null;
      if (context.hasParam(ConstraintsParameterMapping.PARAM_LAT)
          && context.hasParam(ConstraintsParameterMapping.PARAM_LON)) {
        try {
          double lat = Double.parseDouble(context.getParam(ConstraintsParameterMapping.PARAM_LAT));
          double lon = Double.parseDouble(context.getParam(ConstraintsParameterMapping.PARAM_LON));
          location = LatLng.newInstance(lat, lon);
        } catch (NumberFormatException ex) {
          System.err.println("error parsing lat and lon: lat="
              + context.getParam(ConstraintsParameterMapping.PARAM_LAT)
              + " lon="
              + context.getParam(ConstraintsParameterMapping.PARAM_LON));
        }
      }

      TransitShedConstraintsBean constraints = new TransitShedConstraintsBean();
      constraints.setMaxInitialWaitTime(60*60);
      ConstraintsParameterMapping.addParamsToConstraints(context, constraints);

      long time = ConstraintsParameterMapping.getParamsAsTime(context);
      if (time == 0)
        time = System.currentTimeMillis();

      _queryModel.setQuery(query, locationQuery, location, time, constraints);
      _stateEvents.fireModelChange(new StateEvent(new SearchStartedState()));
    }
  }

  public void setQueryLocation(LatLng point) {
    String query = _queryModel.getQuery();
    long time = _queryModel.getTime();
    TransitShedConstraintsBean constraints = _queryModel.getConstraints();
    query(query, "", point, time, constraints);
  }

  public void search(MinTransitTimeResult result) {

    _resultsModel.clear();

    System.out.println("here?");

    String query = _queryModel.getQuery();
    String category = "";

    if (query.equals("NOTHING") || query.length() == 0)
      return;

    TransitShedConstraintsBean constraints = _queryModel.getConstraints();

    LocalSearchHandler handler = new LocalSearchHandler(constraints, result);
    handler.setEventSink(_stateEvents);
    handler.setLocalSearchProvider(_localSearchProvider);
    handler.setModel(_resultsModel);
    handler.addQuery(query, category);

    handler.run();
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  @Override
  protected LatLng getQueryLocation() {
    return _queryModel.getLocation();
  }

  @Override
  protected long getQueryTime() {
    return _queryModel.getTime();
  }

  @Override
  protected TransitShedConstraintsBean getQueryConstraints() {
    return _queryModel.getConstraints();
  }

  @Override
  protected void setQueryLocationLookupResult(Place place) {
    _queryModel.setQueryLocation(place.getLocation());
  }

}
