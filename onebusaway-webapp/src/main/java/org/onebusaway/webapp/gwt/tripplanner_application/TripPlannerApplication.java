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
package org.onebusaway.webapp.gwt.tripplanner_application;

import org.onebusaway.webapp.gwt.tripplanner_application.control.GeocoderHandlers;
import org.onebusaway.webapp.gwt.tripplanner_application.control.TripPlannerResultHandler;
import org.onebusaway.webapp.gwt.tripplanner_application.resources.Resources;
import org.onebusaway.webapp.gwt.tripplanner_application.view.MainWidget;
import org.onebusaway.webapp.gwt.tripplanner_application.view.SearchWidget;
import org.onebusaway.webapp.gwt.tripplanner_library.model.TripPlanModel;
import org.onebusaway.webapp.gwt.tripplanner_library.resources.TripPlannerResources;
import org.onebusaway.webapp.gwt.tripplanner_library.view.TripPlanResultTablePresenter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.ui.RootPanel;

public class TripPlannerApplication implements EntryPoint {

  private static long _time = System.currentTimeMillis();

  public static void log(String message) {
    long t = System.currentTimeMillis() - _time;
    System.out.println("t=" + t + " msg=" + message);
  }

  public void onModuleLoad() {

    RootPanel root = RootPanel.get("root");

    TripPlanModel model = new TripPlanModel();

    TripPlannerResultHandler resultsHandler = new TripPlannerResultHandler();
    resultsHandler.setModel(model);

    GeocoderHandlers geocoderHandler = new GeocoderHandlers();
    geocoderHandler.setTripPlannerResultHandler(resultsHandler);

    MapWidget map = new MapWidget(LatLng.newInstance(47.601533, -122.32933), 11);
    map.addControl(new LargeMapControl());

    SearchWidget searchWidget = new SearchWidget();
    searchWidget.setGeocoderHandler(geocoderHandler);
    searchWidget.setMapWidget(map);

    geocoderHandler.setSearchWidget(searchWidget);

    TripPlanResultTablePresenter resultsWidget = new TripPlanResultTablePresenter();
    model.addModelListener(resultsWidget);
    resultsWidget.setMapWidget(map);

    MainWidget mainWidget = new MainWidget();
    mainWidget.setMapWidget(map);
    mainWidget.setSearchWidget(searchWidget);
    mainWidget.setResultsWidget(resultsWidget);

    mainWidget.initialize();
    searchWidget.initialize();

    root.add(mainWidget);

    StyleInjector.injectStylesheet(TripPlannerResources.INSTANCE.getCss().getText());
    StyleInjector.injectStylesheet(Resources.INSTANCE.getCSS().getText());
  }
}
