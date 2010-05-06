package org.onebusaway.webapp.gwt.oba_application.control;

import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.oba.OneBusAwayConstraintsBean;
import org.onebusaway.transit_data.model.tripplanner.TripPlanBean;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextHelper;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.control.PlaceSearch;
import org.onebusaway.webapp.gwt.common.control.PlaceSearchListener;
import org.onebusaway.webapp.gwt.common.model.ModelEventSink;
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.oba_application.control.state.AddressLookupErrorState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchCompleteState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchLocationUpdatedState;
import org.onebusaway.webapp.gwt.oba_application.control.state.State;
import org.onebusaway.webapp.gwt.oba_application.control.state.TripPlansState;
import org.onebusaway.webapp.gwt.oba_application.model.FilteredResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.LocationQueryModel;
import org.onebusaway.webapp.gwt.oba_application.model.PagedResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.ResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.TimedLocalSearchResult;
import org.onebusaway.webapp.gwt.oba_application.search.LocalSearchProvider;
import org.onebusaway.webapp.gwt.tripplanner_library.model.TripPlanModel;
import org.onebusaway.webapp.gwt.where_library.rpc.WebappServiceAsync;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonControlImpl implements CommonControl {

  protected ModelEventSink<StateEvent> _stateEvents;

  /*****************************************************************************
   * Model Layer
   ****************************************************************************/

  protected ResultsModel _resultsModel;

  private FilteredResultsModel _filteredResultsModel;

  private PagedResultsModel _pagedResultsModel;

  private TripPlanModel _tripModel;

  /****
   * Control Layer
   ****/

  protected ContextHelper _contextHelper;

  private MinTransitTimeResultHandler _minTransitTimeResultHandler;

  protected LocalSearchProvider _localSearchProvider;

  protected int _contextIndex = 0;

  public void setStateEvents(ModelEventSink<StateEvent> events) {
    _stateEvents = events;
  }

  public void setContextManager(ContextManager contextManager) {
    _contextHelper = new ContextHelper(contextManager);
  }

  public ModelListener<LocationQueryModel> getQueryModelHandler() {
    return new LocationQueryHandler();
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

  public StateEventListener getStateEventListener() {
    return new StateEventHandler();
  }

  /*****************************************************************************
   * {@link OneBusAwayStandardControl} Interface
   ****************************************************************************/

  public void handleContext(Context context) {

    if (context.hasParam("_")) {
      try {
        _contextIndex = Integer.parseInt(context.getParam("_")) + 1;
      } catch (NumberFormatException ex) {

      }
    }
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

    WebappServiceAsync service = WebappServiceAsync.SERVICE;

    LatLng fromPoint = getQueryLocation();
    double latFrom = fromPoint.getLatitude();
    double lonFrom = fromPoint.getLongitude();
    double latTo = place.getLat();
    double lonTo = place.getLon();
    OneBusAwayConstraintsBean constraints = getQueryConstraints();

    service.getTripsBetween(latFrom, lonFrom, latTo, lonTo, constraints,
        new TripPlanHandler());
  }

  /****
   * Protected Methods
   ****/

  protected abstract LatLng getQueryLocation();

  protected abstract OneBusAwayConstraintsBean getQueryConstraints();

  protected abstract void setQueryLocationLookupResult(Place place);

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private class LocationQueryHandler implements
      ModelListener<LocationQueryModel> {

    public void handleUpdate(LocationQueryModel model) {

      if (model.getLocation() == null) {

        PlaceSearch search = new PlaceSearch();

        LatLngBounds view = LatLngBounds.newInstance();
        view.extend(LatLng.newInstance(47.97430795395781, -121.79454591726969));
        view.extend(LatLng.newInstance(47.152554314370924, -122.50104172828858));

        search.query(model.getLocationQuery(), new GeocoderResultHandler(),
            view);

      } else {

        _stateEvents.fireModelChange(new StateEvent(
            new SearchLocationUpdatedState()));

        LatLng location = model.getLocation();
        OneBusAwayConstraintsBean constraints = model.getConstraints();
        WebappServiceAsync service = WebappServiceAsync.SERVICE;

        int timeSegmentSize = (constraints.getMaxTripDuration() / 10);

        service.getMinTravelTimeToStopsFrom(location.getLatitude(),
            location.getLongitude(), constraints, timeSegmentSize,
            _minTransitTimeResultHandler);
      }
    }
  }

  private class GeocoderResultHandler implements PlaceSearchListener {

    public void handleSingleResult(Place place) {
      setQueryLocationLookupResult(place);
    }

    public void handleNoResult() {
      _stateEvents.fireModelChange(new StateEvent(new AddressLookupErrorState(
          new ArrayList<Place>())));
    }

    public void handleMultipleResults(List<Place> locations) {
      _stateEvents.fireModelChange(new StateEvent(new AddressLookupErrorState(
          locations)));
    }

    public void handleError() {
      _stateEvents.fireModelChange(new StateEvent(new AddressLookupErrorState(
          new ArrayList<Place>())));
    }

  }

  private class StateEventHandler implements StateEventListener {

    public void handleUpdate(StateEvent model) {

      State state = model.getState();

      if (state instanceof SearchCompleteState) {

        WebappServiceAsync service = WebappServiceAsync.SERVICE;
        service.clearCurrentMinTravelTimeResults(new AsyncCallback<Void>() {

          public void onFailure(Throwable ex) {
            System.err.println("error clearing current result");
            ex.printStackTrace();
          }

          public void onSuccess(Void v) {

          }
        });

      }
    }

  }

  private class TripPlanHandler implements AsyncCallback<List<TripPlanBean>> {

    public void onSuccess(List<TripPlanBean> trips) {
      _tripModel.setTripPlans(trips);
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
    }
  }
}
