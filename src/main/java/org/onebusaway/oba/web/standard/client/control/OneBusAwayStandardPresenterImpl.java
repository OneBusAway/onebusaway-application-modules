package org.onebusaway.oba.web.standard.client.control;

import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.common.web.common.client.context.ContextHelper;
import org.onebusaway.common.web.common.client.context.HistoryContextManager;
import org.onebusaway.common.web.common.client.control.GeocoderHelper;
import org.onebusaway.common.web.common.client.control.GeocoderResultListener;
import org.onebusaway.common.web.common.client.model.ModelEventSink;
import org.onebusaway.common.web.common.client.model.ModelListener;
import org.onebusaway.oba.web.common.client.model.LocalSearchResult;
import org.onebusaway.oba.web.common.client.model.LocationBounds;
import org.onebusaway.oba.web.common.client.model.OneBusAwayConstraintsBean;
import org.onebusaway.oba.web.common.client.rpc.OneBusAwayWebServiceAsync;
import org.onebusaway.oba.web.standard.client.control.state.AddressLookupErrorState;
import org.onebusaway.oba.web.standard.client.control.state.SearchLocationUpdatedState;
import org.onebusaway.oba.web.standard.client.control.state.SearchStartedState;
import org.onebusaway.oba.web.standard.client.control.state.TripPlansState;
import org.onebusaway.oba.web.standard.client.model.FilteredResultsModel;
import org.onebusaway.oba.web.standard.client.model.ResultsModel;
import org.onebusaway.oba.web.standard.client.model.PagedResultsModel;
import org.onebusaway.oba.web.standard.client.model.QueryModel;
import org.onebusaway.oba.web.standard.client.model.TimedLocalSearchResult;
import org.onebusaway.oba.web.standard.client.search.LocalSearchProvider;
import org.onebusaway.tripplanner.web.common.client.model.TripBean;
import org.onebusaway.tripplanner.web.common.client.model.TripPlanModel;
import org.onebusaway.tripplanner.web.common.client.rpc.TripPlannerWebServiceAsync;

import com.google.gwt.maps.client.geocode.Placemark;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OneBusAwayStandardPresenterImpl implements OneBusAwayStandardPresenter {

  private ModelEventSink<StateEvent> _stateEvents;

  /*****************************************************************************
   * Model Layer
   ****************************************************************************/

  private QueryModel _queryModel;

  private ResultsModel _resultsModel;

  private FilteredResultsModel _filteredResultsModel;

  private PagedResultsModel _pagedResultsModel;

  private TripPlanModel _tripModel;

  /****
   * Control Layer
   ****/

  private ContextHelper _contextHelper = new ContextHelper(new HistoryContextManager());

  private MinTransitTimeResultHandler _minTransitTimeResultHandler;

  private LocalSearchProvider _localSearchProvider;

  private int _contextIndex = 0;

  public void setStateEvents(ModelEventSink<StateEvent> events) {
    _stateEvents = events;
  }

  public ModelListener<QueryModel> getQueryModelHandler() {
    return new QueryHandler();
  }

  public void setQueryModel(QueryModel constraintsModel) {
    _queryModel = constraintsModel;
  }

  public void setResultsModel(ResultsModel model) {
    _resultsModel = model;
  }

  public void setFilteredResultsModel(FilteredResultsModel filteredResultsModel) {
    _filteredResultsModel = filteredResultsModel;
  }

  public void setPagedResultsModel(PagedResultsModel model) {
    _pagedResultsModel = model;
  }

  public void setTripPlanModel(TripPlanModel tripModel) {
    _tripModel = tripModel;
  }

  public void setMinTransitTimeResultHandler(MinTransitTimeResultHandler handler) {
    _minTransitTimeResultHandler = handler;
  }

  public void setLocalSearchProvider(LocalSearchProvider provider) {
    _localSearchProvider = provider;
  }

  /*****************************************************************************
   * {@link OneBusAwayStandardPresenter} Interface
   ****************************************************************************/

  public void handleContext(Context context) {

    if (context.hasParam("_")) {
      try {
        _contextIndex = Integer.parseInt(context.getParam("_")) + 1;
      } catch (NumberFormatException ex) {

      }
    }

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
              + context.getParam(ConstraintsParameterMapping.PARAM_LAT) + " lon="
              + context.getParam(ConstraintsParameterMapping.PARAM_LON));
        }
      }

      OneBusAwayConstraintsBean constraints = new OneBusAwayConstraintsBean();
      ConstraintsParameterMapping.addParamsToConstraints(context, constraints);

      _queryModel.setQuery(query, locationQuery, location, constraints);
      _stateEvents.fireModelChange(new StateEvent(new SearchStartedState()));
    }

  }

  public void query(String query, String locationQuery, LatLng location, OneBusAwayConstraintsBean constraints) {

    // We push the query onto the context so it can be bookmarked
    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put(ConstraintsParameterMapping.PARAM_QUERY, query);
    if (locationQuery != null && locationQuery.length() > 0)
      params.put(ConstraintsParameterMapping.PARAM_LOCATION, locationQuery);
    if (location != null) {
      params.put(ConstraintsParameterMapping.PARAM_LAT, Double.toString(location.getLatitude()));
      params.put(ConstraintsParameterMapping.PARAM_LON, Double.toString(location.getLongitude()));
    }

    ConstraintsParameterMapping.addConstraintsToParams(constraints, params);

    params.put("_", Integer.toString(_contextIndex++));

    _contextHelper.setContext(params);
  }

  public void setQueryLocation(LatLng point) {
    String query = _queryModel.getQuery();
    OneBusAwayConstraintsBean constraints = _queryModel.getConstraints();
    query(query, "", point, constraints);
  }

  public void search(String resultId, List<LocationBounds> searchBounds) {

    _resultsModel.clear();

    String query = _queryModel.getQuery();
    String category = "";

    LocalSearchHandler handler = new LocalSearchHandler(resultId, searchBounds, query, category);
    handler.setEventSink(_stateEvents);
    handler.setLocalSearchProvider(_localSearchProvider);
    handler.setModel(_resultsModel);

    handler.run();
  }

  public void filterResults(Filter<TimedLocalSearchResult> filter) {
    _filteredResultsModel.setFilter(filter);
  }

  public void setActiveSearchResult(TimedLocalSearchResult result) {
    _pagedResultsModel.setSelectedResult(result);
  }

  public void clearActiveSearchResult() {
    _pagedResultsModel.clearActiveSearchResult();
  }

  public void getDirectionsToPlace(LocalSearchResult place) {

    _stateEvents.fireModelChange(new StateEvent(new TripPlansState()));

    TripPlannerWebServiceAsync service = TripPlannerWebServiceAsync.SERVICE;

    LatLng fromPoint = _queryModel.getLocation();
    double latFrom = fromPoint.getLatitude();
    double lonFrom = fromPoint.getLongitude();
    double latTo = place.getLat();
    double lonTo = place.getLon();
    OneBusAwayConstraintsBean constraints = _queryModel.getConstraints();

    service.getTripsBetween(latFrom, lonFrom, latTo, lonTo, constraints, new TripPlanHandler());
  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private class QueryHandler implements ModelListener<QueryModel> {

    public void handleUpdate(QueryModel model) {

      if (model.getLocation() == null) {

        GeocoderHelper helper = new GeocoderHelper();

        LatLngBounds view = LatLngBounds.newInstance();
        view.extend(LatLng.newInstance(47.97430795395781, -121.79454591726969));
        view.extend(LatLng.newInstance(47.152554314370924, -122.50104172828858));

        helper.setViewport(view);
        helper.setListener(new GeocoderResultHandler());
        helper.query(model.getLocationQuery());

      } else {

        _stateEvents.fireModelChange(new StateEvent(new SearchLocationUpdatedState()));

        LatLng location = model.getLocation();
        OneBusAwayConstraintsBean constraints = model.getConstraints();
        OneBusAwayWebServiceAsync service = OneBusAwayWebServiceAsync.SERVICE;
        service.getMinTravelTimeToStopsFrom(location.getLatitude(), location.getLongitude(), constraints,
            _minTransitTimeResultHandler);
      }
    }
  }

  private class GeocoderResultHandler implements GeocoderResultListener {

    public void setQueryLocation(LatLng location) {
      _queryModel.setQueryLocation(location);
    }

    public void setNoQueryLocations() {
      _stateEvents.fireModelChange(new StateEvent(new AddressLookupErrorState(new ArrayList<Placemark>())));
    }

    public void setTooManyQueryLocations(List<Placemark> locations) {
      _stateEvents.fireModelChange(new StateEvent(new AddressLookupErrorState(locations)));
    }

    public void setErrorOnQueryLocation() {
      _stateEvents.fireModelChange(new StateEvent(new AddressLookupErrorState(new ArrayList<Placemark>())));
    }
  }

  private class TripPlanHandler implements AsyncCallback<List<TripBean>> {

    public void onSuccess(List<TripBean> trips) {
      _tripModel.setTripPlans(trips);
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
    }
  }

}
