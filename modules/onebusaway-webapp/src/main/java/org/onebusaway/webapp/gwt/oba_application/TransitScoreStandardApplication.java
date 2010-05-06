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
package org.onebusaway.webapp.gwt.oba_application;

import org.onebusaway.webapp.gwt.common.AbstractSinglePageApplication;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.common.layout.BoxLayoutManager;
import org.onebusaway.webapp.gwt.common.model.ModelEventSourceAndSink;
import org.onebusaway.webapp.gwt.common.model.ModelEventsImpl;
import org.onebusaway.webapp.gwt.common.model.ModelEventsSourceAndSink;
import org.onebusaway.webapp.gwt.oba_application.control.MinTransitTimeResultHandler;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.control.TransitScoreControlImpl;
import org.onebusaway.webapp.gwt.oba_application.model.FilteredResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.LocationQueryModel;
import org.onebusaway.webapp.gwt.oba_application.model.PagedResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.ResultsModel;
import org.onebusaway.webapp.gwt.oba_application.resources.OneBusAwayStandardResources;
import org.onebusaway.webapp.gwt.oba_application.search.YelpLocalSearchProvider;
import org.onebusaway.webapp.gwt.oba_application.view.ActiveResultPresenter;
import org.onebusaway.webapp.gwt.oba_application.view.AddressLookupPresenter;
import org.onebusaway.webapp.gwt.oba_application.view.CustomTimedOverlayManager;
import org.onebusaway.webapp.gwt.oba_application.view.CustomTripPlanResultTablePresenter;
import org.onebusaway.webapp.gwt.oba_application.view.ExceptionPage;
import org.onebusaway.webapp.gwt.oba_application.view.MainPage;
import org.onebusaway.webapp.gwt.oba_application.view.ResultsFilterPresenter;
import org.onebusaway.webapp.gwt.oba_application.view.ResultsTablePresenter;
import org.onebusaway.webapp.gwt.oba_application.view.SearchOriginMapPresenter;
import org.onebusaway.webapp.gwt.oba_application.view.TransitScorePresenter;
import org.onebusaway.webapp.gwt.oba_application.view.TransitScoreSearchPresenter;
import org.onebusaway.webapp.gwt.oba_application.view.TransitScoreWelcomePagePresenter;
import org.onebusaway.webapp.gwt.tripplanner_library.model.TripPlanModel;

import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.geom.LatLng;

public class TransitScoreStandardApplication extends AbstractSinglePageApplication {

  public TransitScoreStandardApplication() {

    ModelEventsSourceAndSink events = new ModelEventsImpl();

    ModelEventSourceAndSink<LocationQueryModel> locationQueryModelEvents = events.getEventSourceAndSink(LocationQueryModel.class);
    ModelEventSourceAndSink<ResultsModel> resultsModelEvents = events.getEventSourceAndSink(ResultsModel.class);
    ModelEventSourceAndSink<FilteredResultsModel> filteredResultsModelEvents = events.getEventSourceAndSink(FilteredResultsModel.class);
    ModelEventSourceAndSink<StateEvent> stateEvents = events.getEventSourceAndSink(StateEvent.class);

    /**
     * Model Layer
     */
    LocationQueryModel queryModel = new LocationQueryModel();
    queryModel.setEventSink(locationQueryModelEvents);

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
    //minTransitTimeHandler.setTimedRegionModel(timedRegionModel);

    TransitScoreControlImpl control = new TransitScoreControlImpl();

    // Models
    control.setStateEvents(stateEvents);
    control.setQueryModel(queryModel);
    control.setResultsModel(resultsModel);
    control.setFilteredResultsModel(filteredResultsModel);
    control.setPagedResultsModel(pagedResultsModel);
    control.setTripPlanModel(tripModel);
    // Controls
    control.setLocalSearchProvider(localSearchProvider);
    control.setMinTransitTimeResultHandler(minTransitTimeHandler);

    minTransitTimeHandler.setControl(control);

    /**
     * View Layer
     */
    MapWidget mapWidget = new MapWidget(LatLng.newInstance(47.601533, -122.32933), 11);
    mapWidget.addControl(new LargeMapControl());
    mapWidget.addControl(new MapTypeControl());
    mapWidget.addControl(new ScaleControl());

    mapOverlayManager.setMapWidget(mapWidget);

    BoxLayoutManager layoutManager = new BoxLayoutManager();

    TransitScoreSearchPresenter search = new TransitScoreSearchPresenter();
    search.setControl(control);
    search.setMapWidget(mapWidget);
    search.setLayoutManager(layoutManager);

    TransitScoreWelcomePagePresenter welcome = new TransitScoreWelcomePagePresenter();

    AddressLookupPresenter addressLookup = new AddressLookupPresenter();
    addressLookup.setControl(control);

    TransitScorePresenter score = new TransitScorePresenter();
    score.setTransitScoreControl(control);

    ResultsFilterPresenter filter = new ResultsFilterPresenter();
    filter.setControl(control);

    ResultsTablePresenter resultsTable = new ResultsTablePresenter();
    resultsTable.setControl(control);
    resultsTable.setResultsModel(pagedResultsModel);
    resultsTable.setMapOverlayManager(mapOverlayManager);
    resultsTable.setResultsFilterWidget(filter.getWidget());

    CustomTimedOverlayManager timedRegionOverlayManager = new CustomTimedOverlayManager();
    timedRegionOverlayManager.setMapOverlayManager(mapOverlayManager);

    CustomTripPlanResultTablePresenter plansWidget = new CustomTripPlanResultTablePresenter();
    plansWidget.setMapWidget(mapWidget);

    ActiveResultPresenter activeResult = new ActiveResultPresenter();
    activeResult.setMapOverlayManager(mapOverlayManager);
    activeResult.setControl(control);

    MainPage mainPage = new MainPage();
    mainPage.setControl(control);
    mainPage.setLayoutManager(layoutManager);
    mainPage.setMapWidget(mapWidget);
    mainPage.setSearchWidget(search);
    mainPage.addResultsPanelWidget(welcome.getWidget());
    mainPage.addResultsPanelWidget(addressLookup.getWidget());
    mainPage.addResultsPanelWidget(score.getWidget());
    mainPage.addResultsPanelWidget(resultsTable.getWidget());
    mainPage.addResultsPanelWidget(activeResult.getWidget());
    mainPage.addResultsPanelWidget(plansWidget.getWidget());

    SearchOriginMapPresenter searchOriginMapMarker = new SearchOriginMapPresenter();
    searchOriginMapMarker.setMapOverlayManager(mapOverlayManager);
    searchOriginMapMarker.setLocationQueryModel(queryModel);

    /**
     * Wire up all the listeners
     */

    //timedRegionModel.addModelListener(timedRegionOverlayManager);

    locationQueryModelEvents.addModelListener(control.getQueryModelHandler());
    locationQueryModelEvents.addModelListener(search.getQueryModelHandler());

    resultsModelEvents.addModelListener(filteredResultsModel.getResultsModelHandler());
    resultsModelEvents.addModelListener(filter.getResultsModelHandler());

    filteredResultsModelEvents.addModelListener(pagedResultsModel.getModelListener());

    tripModel.addModelListener(plansWidget);

    stateEvents.addModelListener(control.getStateEventListener());
    stateEvents.addModelListener(welcome.getStateEventListener());
    stateEvents.addModelListener(resultsTable);
    stateEvents.addModelListener(activeResult);
    stateEvents.addModelListener(searchOriginMapMarker);
    stateEvents.addModelListener(plansWidget.getStateEventHandler());
    stateEvents.addModelListener(timedRegionOverlayManager.getStateEventHandler());
    stateEvents.addModelListener(addressLookup.getStateEventListener());
    stateEvents.addModelListener(score.getStateEventListener());

    setPage(mainPage);
    setExceptionPage(new ExceptionPage());

    StyleInjector.injectStylesheet(OneBusAwayStandardResources.INSTANCE.getCss().getText());
  }
}
