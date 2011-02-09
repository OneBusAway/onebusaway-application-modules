package org.onebusaway.webapp.gwt.oba_application.control;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.MinTransitTimeResult;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchStartedState;
import org.onebusaway.webapp.gwt.oba_application.model.LocationQueryModel;
import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;

import com.google.gwt.maps.client.geom.LatLng;

public class TransitScoreControlImpl extends CommonControlImpl implements
    TransitScoreControl {

  private LocationQueryModel _locationQueryModel;

  private Map<String, Set<String>> _categories = new HashMap<String, Set<String>>();

  public TransitScoreControlImpl() {
    addCategory("restaurants", "Restaurants");
    addCategory("coffee", "Coffee & Tea");
    addCategory("grocery stores", "Grocery");
    addCategory("bars", "Bars");
    addCategory("parks", "Parks");
    addCategory("libraries", "Libraries");
    addCategory("schools", "Elementary Schools", "Preschools",
        "Middle Schools & High Schools");
  }

  public void setQueryModel(LocationQueryModel model) {
    _locationQueryModel = model;
  }

  public void query(String locationQuery, LatLng location,
      OneBusAwayConstraintsBean constraints) {

    // We push the query onto the context so it can be bookmarked
    Map<String, String> params = new LinkedHashMap<String, String>();
    if (locationQuery != null && locationQuery.length() > 0)
      params.put(ConstraintsParameterMapping.PARAM_LOCATION, locationQuery);
    if (location != null) {
      params.put(ConstraintsParameterMapping.PARAM_LAT,
          Double.toString(location.getLatitude()));
      params.put(ConstraintsParameterMapping.PARAM_LON,
          Double.toString(location.getLongitude()));
    }

    ConstraintsParameterMapping.addConstraintsToParams(constraints, params);

    params.put("_", Integer.toString(_contextIndex++));

    _contextHelper.setContext(params);
  }

  public void handleContext(Context context) {

    super.handleContext(context);

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
            + context.getParam(ConstraintsParameterMapping.PARAM_LAT) + " lon="
            + context.getParam(ConstraintsParameterMapping.PARAM_LON));
      }
    }

    if (location == null && locationQuery == null
        || locationQuery.length() == 0)
      return;

    OneBusAwayConstraintsBean constraints = new OneBusAwayConstraintsBean();
    ConstraintsParameterMapping.addParamsToConstraints(context, constraints);
    constraints.setMaxTripDuration(20);

    _locationQueryModel.setQuery(locationQuery, location, constraints);
    _stateEvents.fireModelChange(new StateEvent(new SearchStartedState()));
  }

  public void setQueryLocation(LatLng point) {
    OneBusAwayConstraintsBean constraints = _locationQueryModel.getConstraints();
    query("", point, constraints);
  }

  public void search(MinTransitTimeResult result) {
    _resultsModel.clear();
    OneBusAwayConstraintsBean constraints = _locationQueryModel.getConstraints();
    LocalSearchHandler handler = new LocalSearchHandler(constraints,result);
    handler.setEventSink(_stateEvents);
    handler.setLocalSearchProvider(_localSearchProvider);
    handler.setModel(_resultsModel);

    for (String name : _categories.keySet())
      handler.addQuery(name, "");

    handler.run();
  }

  public double getCurrentTransitScore() {

    List<TimedLocalSearchResult> results = _resultsModel.getResults();

    Map<String, Integer> maxScoreByCategory = new HashMap<String, Integer>();

    for (TimedLocalSearchResult result : results) {

      int score = scoreSearchResult(result);

      LocalSearchResult local = result.getLocalSearchResult();

      for (String category : local.getCategories()) {
        Integer currentScore = maxScoreByCategory.get(category);
        if (currentScore == null || currentScore < score)
          maxScoreByCategory.put(category, score);
      }
    }

    double totalScore = 0;

    for (Set<String> names : _categories.values()) {
      int max = 0;
      for (String name : names) {
        if (maxScoreByCategory.containsKey(name))
          max = Math.max(max, maxScoreByCategory.get(name));
      }
      totalScore += max;
    }

    totalScore /= (_categories.size() * 10);

    return totalScore;

  }

  private int scoreSearchResult(TimedLocalSearchResult result) {
    int index = (result.getTime() / 60) / 6;
    int score = 0;
    switch (index) {
      case 0:
        score = 10;
        break;
      case 1:
        score = 8;
        break;
      case 2:
        score = 4;
        break;
      case 3:
        score = 2;
        break;
    }
    return score;
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  @Override
  protected LatLng getQueryLocation() {
    return _locationQueryModel.getLocation();
  }

  @Override
  protected OneBusAwayConstraintsBean getQueryConstraints() {
    return _locationQueryModel.getConstraints();
  }

  @Override
  protected void setQueryLocationLookupResult(Place place) {
    _locationQueryModel.setQueryLocation(place.getLocation());
  }

  private void addCategory(String category, String... otherNames) {
    Set<String> names = new HashSet<String>();
    for (String name : otherNames)
      names.add(name);
    _categories.put(category, names);
  }
}
