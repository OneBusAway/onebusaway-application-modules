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
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.oba.LocalSearchResult;
import org.onebusaway.transit_data.model.tripplanning.ConstraintsBean;
import org.onebusaway.transit_data.model.tripplanning.ItinerariesBean;
import org.onebusaway.transit_data.model.tripplanning.TransitShedConstraintsBean;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextHelper;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.control.Place;
import org.onebusaway.webapp.gwt.common.control.PlaceSearch;
import org.onebusaway.webapp.gwt.common.control.PlaceSearchListener;
import org.onebusaway.webapp.gwt.common.model.ModelEventSink;
import org.onebusaway.webapp.gwt.common.model.ModelListener;
import org.onebusaway.webapp.gwt.oba_application.control.state.AddressLookupErrorState;
import org.onebusaway.webapp.gwt.oba_application.control.state.SearchLocationUpdatedState;
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
    CoordinatePoint from = new CoordinatePoint(fromPoint.getLatitude(),
        fromPoint.getLongitude());
    CoordinatePoint to = new CoordinatePoint(place.getLat(), place.getLon());
    long time = getQueryTime();
    TransitShedConstraintsBean constraints = getQueryConstraints();

    service.getTripsBetween(from, to, time, constraints.getConstraints(),
        new TripPlanHandler());
  }

  /****
   * Protected Methods
   ****/

  protected abstract LatLng getQueryLocation();

  protected abstract long getQueryTime();

  protected abstract TransitShedConstraintsBean getQueryConstraints();

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
        long time = model.getTime();
        TransitShedConstraintsBean constraints = model.getConstraints();
        ConstraintsBean c = constraints.getConstraints();
        WebappServiceAsync service = WebappServiceAsync.SERVICE;

        int timeSegmentSize = (c.getMaxTripDuration() /  600);

        CoordinatePoint p = new CoordinatePoint(location.getLatitude(),
            location.getLongitude());
        service.getMinTravelTimeToStopsFrom(p, time, constraints,
            timeSegmentSize, _minTransitTimeResultHandler);
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

  private class TripPlanHandler implements AsyncCallback<ItinerariesBean> {

    public void onSuccess(ItinerariesBean trips) {
      _tripModel.setTripPlans(trips);
    }

    public void onFailure(Throwable ex) {
      ex.printStackTrace();
    }
  }
}
