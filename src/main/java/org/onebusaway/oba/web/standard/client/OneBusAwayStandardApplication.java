/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.oba.web.standard.client;

import org.onebusaway.common.web.common.client.AbstractSinglePageApplication;
import org.onebusaway.common.web.common.client.MapOverlayManager;
import org.onebusaway.common.web.common.client.model.ModelEventSourceAndSink;
import org.onebusaway.common.web.common.client.model.ModelEventsImpl;
import org.onebusaway.common.web.common.client.model.ModelEventsSourceAndSink;
import org.onebusaway.oba.web.common.client.model.TimedRegionModel;
import org.onebusaway.oba.web.standard.client.control.MinTransitTimeResultHandler;
import org.onebusaway.oba.web.standard.client.control.OneBusAwayStandardPresenterImpl;
import org.onebusaway.oba.web.standard.client.control.StateEvent;
import org.onebusaway.oba.web.standard.client.model.FilteredResultsModel;
import org.onebusaway.oba.web.standard.client.model.PagedResultsModel;
import org.onebusaway.oba.web.standard.client.model.QueryModel;
import org.onebusaway.oba.web.standard.client.model.ResultsModel;
import org.onebusaway.oba.web.standard.client.resources.OneBusAwayStandardResources;
import org.onebusaway.oba.web.standard.client.search.YelpLocalSearchProvider;
import org.onebusaway.oba.web.standard.client.view.ActiveResultPresenter;
import org.onebusaway.oba.web.standard.client.view.AddressLookupPresenter;
import org.onebusaway.oba.web.standard.client.view.CustomTimedRegionOverlayManager;
import org.onebusaway.oba.web.standard.client.view.CustomTripPlanResultTablePresenter;
import org.onebusaway.oba.web.standard.client.view.ExceptionPage;
import org.onebusaway.oba.web.standard.client.view.MainPage;
import org.onebusaway.oba.web.standard.client.view.ResultsFilterPresenter;
import org.onebusaway.oba.web.standard.client.view.ResultsTablePresenter;
import org.onebusaway.oba.web.standard.client.view.SearchOriginMapPresenter;
import org.onebusaway.oba.web.standard.client.view.SearchWidget;
import org.onebusaway.tripplanner.web.common.client.model.TripPlanModel;

import com.google.gwt.libideas.client.StyleInjector;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.geom.LatLng;

public class OneBusAwayStandardApplication extends AbstractSinglePageApplication {

  public OneBusAwayStandardApplication() {

    ModelEventsSourceAndSink events = new ModelEventsImpl();
    ModelEventSourceAndSink<QueryModel> queryModelEvents = events.getEventSourceAndSink(QueryModel.class);
    ModelEventSourceAndSink<ResultsModel> resultsModelEvents = events.getEventSourceAndSink(ResultsModel.class);
    ModelEventSourceAndSink<FilteredResultsModel> filteredResultsModelEvents = events.getEventSourceAndSink(FilteredResultsModel.class);
    ModelEventSourceAndSink<StateEvent> stateEvents = events.getEventSourceAndSink(StateEvent.class);

    /**
     * Model Layer
     */
    QueryModel queryModel = new QueryModel();
    queryModel.setEvents(queryModelEvents);

    TimedRegionModel timedRegionModel = new TimedRegionModel();

    ResultsModel resultsModel = new ResultsModel();
    resultsModel.setEvents(resultsModelEvents);

    FilteredResultsModel filteredResultsModel = new FilteredResultsModel();
    filteredResultsModel.setResultsModel(resultsModel);
    filteredResultsModel.setEvents(filteredResultsModelEvents);

    PagedResultsModel pagedResultsModel = new PagedResultsModel();
    pagedResultsModel.setModel(filteredResultsModel);
    pagedResultsModel.setEvents(stateEvents);

    TripPlanModel tripModel = new TripPlanModel();

    /**
     * Control Layer
     */
    YelpLocalSearchProvider localSearchProvider = new YelpLocalSearchProvider("VJYMvEAOfkqDeyHh4C9gTg");
    localSearchProvider.setTimeout(10000);

    MapOverlayManager mapOverlayManager = new MapOverlayManager();

    MinTransitTimeResultHandler minTransitTimeHandler = new MinTransitTimeResultHandler();
    minTransitTimeHandler.setMapOverlayManager(mapOverlayManager);
    minTransitTimeHandler.setTimedRegionModel(timedRegionModel);

    OneBusAwayStandardPresenterImpl control = new OneBusAwayStandardPresenterImpl();
    control.setStateEvents(stateEvents);

    // Models
    control.setQueryModel(queryModel);
    control.setResultsModel(resultsModel);
    control.setFilteredResultsModel(filteredResultsModel);
    control.setPagedResultsModel(pagedResultsModel);
    control.setTripPlanModel(tripModel);
    // Controls
    control.setLocalSearchProvider(localSearchProvider);
    control.setMinTransitTimeResultHandler(minTransitTimeHandler);

    minTransitTimeHandler.setPresenter(control);

    /**
     * View Layer
     */
    MapWidget mapWidget = new MapWidget(LatLng.newInstance(47.601533, -122.32933), 11);
    mapWidget.addControl(new LargeMapControl());
    mapWidget.addControl(new MapTypeControl());
    mapWidget.addControl(new ScaleControl());

    mapOverlayManager.setMapWidget(mapWidget);

    SearchWidget searchWidget = new SearchWidget();
    searchWidget.setPresenter(control);
    searchWidget.setMapWidget(mapWidget);

    AddressLookupPresenter addressLookup = new AddressLookupPresenter();
    addressLookup.setPresenter(control);

    ResultsFilterPresenter filter = new ResultsFilterPresenter();
    filter.setPresenter(control);

    ResultsTablePresenter resultsTable = new ResultsTablePresenter();
    resultsTable.setPresenter(control);
    resultsTable.setResultsModel(pagedResultsModel);
    resultsTable.setMapOverlayManager(mapOverlayManager);
    resultsTable.setResultsFilterWidget(filter.getWidget());

    CustomTimedRegionOverlayManager timedRegionOverlayManager = new CustomTimedRegionOverlayManager();
    timedRegionOverlayManager.setMapOverlayManager(mapOverlayManager);

    CustomTripPlanResultTablePresenter plansWidget = new CustomTripPlanResultTablePresenter();
    plansWidget.setMapWidget(mapWidget);

    ActiveResultPresenter activeResult = new ActiveResultPresenter();
    activeResult.setMapOverlayManager(mapOverlayManager);
    activeResult.setControlLayer(control);

    MainPage mainPage = new MainPage();
    mainPage.setPresenter(control);
    mainPage.setMapWidget(mapWidget);
    mainPage.setSearchWidget(searchWidget);
    mainPage.setAddressLookupWidget(addressLookup.getWidget());
    mainPage.setResultsTableWidget(resultsTable.getWidget());
    mainPage.setActiveResultWidget(activeResult.getWidget());
    mainPage.setTripPlanResultTable(plansWidget.getWidget());

    SearchOriginMapPresenter searchOriginMapMarker = new SearchOriginMapPresenter();
    searchOriginMapMarker.setMapOverlayManager(mapOverlayManager);
    searchOriginMapMarker.setQueryModel(queryModel);

    /**
     * Wire up all the listeners
     */

    timedRegionModel.addModelListener(timedRegionOverlayManager);

    queryModelEvents.addModelListener(control.getQueryModelHandler());
    queryModelEvents.addModelListener(searchWidget.getQueryModelHandler());

    resultsModelEvents.addModelListener(filteredResultsModel.getResultsModelHandler());
    resultsModelEvents.addModelListener(filter.getResultsModelHandler());

    filteredResultsModelEvents.addModelListener(pagedResultsModel.getModelListener());

    tripModel.addModelListener(plansWidget);

    stateEvents.addModelListener(resultsTable);
    stateEvents.addModelListener(activeResult);
    stateEvents.addModelListener(searchOriginMapMarker);
    stateEvents.addModelListener(plansWidget.getStateEventHandler());
    stateEvents.addModelListener(timedRegionOverlayManager.getStateEventHandler());
    stateEvents.addModelListener(addressLookup.getStateEventListener());

    setPage(mainPage);
    setExceptionPage(new ExceptionPage());

    StyleInjector.injectStylesheet(OneBusAwayStandardResources.INSTANCE.getCss().getText());
  }
}
