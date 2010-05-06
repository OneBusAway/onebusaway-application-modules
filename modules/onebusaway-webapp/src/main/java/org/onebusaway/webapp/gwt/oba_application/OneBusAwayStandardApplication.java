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
import org.onebusaway.webapp.gwt.oba_application.control.OneBusAwayStandardControlImpl;
import org.onebusaway.webapp.gwt.oba_application.control.StateEvent;
import org.onebusaway.webapp.gwt.oba_application.model.FilteredResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.LocationQueryModel;
import org.onebusaway.webapp.gwt.oba_application.model.PagedResultsModel;
import org.onebusaway.webapp.gwt.oba_application.model.QueryModel;
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
import org.onebusaway.webapp.gwt.oba_application.view.SearchWidget;
import org.onebusaway.webapp.gwt.oba_application.view.WelcomePagePresenter;
import org.onebusaway.webapp.gwt.oba_library.control.ColorGradientControl;
import org.onebusaway.webapp.gwt.oba_library.model.TimedPolygonModel;
import org.onebusaway.webapp.gwt.oba_library.model.TimedRegionModel;
import org.onebusaway.webapp.gwt.tripplanner_library.model.TripPlanModel;

import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.geom.LatLng;

public class OneBusAwayStandardApplication extends AbstractSinglePageApplication {

  public OneBusAwayStandardApplication() {

  }

  @Override
  public void onModuleLoad() {

    ModelEventsSourceAndSink events = new ModelEventsImpl();
    ModelEventSourceAndSink<LocationQueryModel> locationQueryModelEvents = events.getEventSourceAndSink(LocationQueryModel.class);
    ModelEventSourceAndSink<QueryModel> queryModelEvents = events.getEventSourceAndSink(QueryModel.class);
    ModelEventSourceAndSink<ResultsModel> resultsModelEvents = events.getEventSourceAndSink(ResultsModel.class);
    ModelEventSourceAndSink<FilteredResultsModel> filteredResultsModelEvents = events.getEventSourceAndSink(FilteredResultsModel.class);
    ModelEventSourceAndSink<StateEvent> stateEvents = events.getEventSourceAndSink(StateEvent.class);

    /**
     * Model Layer
     */
    QueryModel queryModel = new QueryModel();
    queryModel.setEventSink(locationQueryModelEvents);
    queryModel.setQueryModelEventSink(queryModelEvents);

    TimedRegionModel timedRegionModel = new TimedRegionModel();
    TimedPolygonModel timedPolygonModel = new TimedPolygonModel();

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
    // minTransitTimeHandler.setTimedRegionModel(timedRegionModel);
    minTransitTimeHandler.setTimedPolygonModel(timedPolygonModel);

    OneBusAwayStandardControlImpl control = new OneBusAwayStandardControlImpl();
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

    control.setContextManager(getContextManager());

    minTransitTimeHandler.setControl(control);

    /**
     * View Layer
     */
    MapWidget mapWidget = new MapWidget(LatLng.newInstance(47.601533, -122.32933), 11);
    mapWidget.addControl(new LargeMapControl());
    mapWidget.addControl(new MapTypeControl());
    mapWidget.addControl(new ScaleControl());
    
    ColorGradientControl colorGradient = new ColorGradientControl();
    mapWidget.addControl(colorGradient);
    
    mapOverlayManager.setMapWidget(mapWidget);

    BoxLayoutManager layoutManager = new BoxLayoutManager();

    SearchWidget searchWidget = new SearchWidget();
    searchWidget.setControl(control);
    searchWidget.setMapWidget(mapWidget);
    searchWidget.setLayoutManager(layoutManager);

    WelcomePagePresenter welcome = new WelcomePagePresenter();

    AddressLookupPresenter addressLookup = new AddressLookupPresenter();
    addressLookup.setControl(control);

    ResultsFilterPresenter filter = new ResultsFilterPresenter();
    filter.setControl(control);

    ResultsTablePresenter resultsTable = new ResultsTablePresenter();
    resultsTable.setControl(control);
    resultsTable.setResultsModel(pagedResultsModel);
    resultsTable.setMapOverlayManager(mapOverlayManager);
    resultsTable.setResultsFilterWidget(filter.getWidget());

    CustomTimedOverlayManager timedOverlayManager = new CustomTimedOverlayManager();
    timedOverlayManager.setMapOverlayManager(mapOverlayManager);
    timedOverlayManager.setQueryModel(queryModel);
    timedOverlayManager.setColorGradientControl(colorGradient);
    
    CustomTripPlanResultTablePresenter plansWidget = new CustomTripPlanResultTablePresenter();
    plansWidget.setMapWidget(mapWidget);

    ActiveResultPresenter activeResult = new ActiveResultPresenter();
    activeResult.setMapOverlayManager(mapOverlayManager);
    activeResult.setControl(control);

    MainPage mainPage = new MainPage();
    mainPage.setControl(control);
    mainPage.setLayoutManager(layoutManager);
    mainPage.setMapWidget(mapWidget);
    mainPage.setSearchWidget(searchWidget);
    mainPage.addResultsPanelWidget(welcome.getWidget());
    mainPage.addResultsPanelWidget(addressLookup.getWidget());
    mainPage.addResultsPanelWidget(resultsTable.getWidget());
    mainPage.addResultsPanelWidget(activeResult.getWidget());
    mainPage.addResultsPanelWidget(plansWidget.getWidget());

    SearchOriginMapPresenter searchOriginMapMarker = new SearchOriginMapPresenter();
    searchOriginMapMarker.setMapOverlayManager(mapOverlayManager);
    searchOriginMapMarker.setLocationQueryModel(queryModel);

    /**
     * Wire up all the listeners
     */

    timedRegionModel.addModelListener(timedOverlayManager.getRegionModelListener());
    timedPolygonModel.addModelListener(timedOverlayManager.getPolygonModelListener());

    locationQueryModelEvents.addModelListener(control.getQueryModelHandler());
    queryModelEvents.addModelListener(searchWidget.getQueryModelHandler());

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
    stateEvents.addModelListener(timedOverlayManager.getStateEventHandler());
    stateEvents.addModelListener(addressLookup.getStateEventListener());

    setPage(mainPage);
    setExceptionPage(new ExceptionPage());

    StyleInjector.injectStylesheet(OneBusAwayStandardResources.INSTANCE.getCss().getText());

    super.onModuleLoad();
  }
}
