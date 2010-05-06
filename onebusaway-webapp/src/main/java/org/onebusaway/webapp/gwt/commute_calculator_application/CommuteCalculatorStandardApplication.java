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
package org.onebusaway.webapp.gwt.commute_calculator_application;

import org.onebusaway.webapp.gwt.common.AbstractSinglePageApplication;
import org.onebusaway.webapp.gwt.common.MapOverlayManager;
import org.onebusaway.webapp.gwt.commute_calculator_application.control.ControlImpl;
import org.onebusaway.webapp.gwt.commute_calculator_application.resources.CommuteCalculatorStandardResources;
import org.onebusaway.webapp.gwt.commute_calculator_application.view.MainPage;
import org.onebusaway.webapp.gwt.commute_calculator_application.view.SearchWidget;
import org.onebusaway.webapp.gwt.oba_library.control.TimedOverlayManager;
import org.onebusaway.webapp.gwt.oba_library.model.TimedPolygonModel;

import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.control.MapTypeControl;
import com.google.gwt.maps.client.control.ScaleControl;
import com.google.gwt.maps.client.geom.LatLng;

public class CommuteCalculatorStandardApplication extends AbstractSinglePageApplication {

  public CommuteCalculatorStandardApplication() {

    /**
     * Model Layer
     */

    TimedPolygonModel model = new TimedPolygonModel();
    
    /**
     * View Layer
     */
    MapWidget mapWidget = new MapWidget(LatLng.newInstance(47.601533, -122.32933), 11);
    mapWidget.addControl(new LargeMapControl());
    mapWidget.addControl(new MapTypeControl());
    mapWidget.addControl(new ScaleControl());
    
    MapOverlayManager mapOverlayManager = new MapOverlayManager();
    mapOverlayManager.setMapWidget(mapWidget);

    SearchWidget searchWidget = new SearchWidget();

    MainPage mainPage = new MainPage();
    mainPage.setMapWidget(mapWidget);
    mainPage.setSearchWidget(searchWidget);

    /**
     * Control Layer
     */

    ControlImpl control = new ControlImpl();
    control.setModel(model);

    searchWidget.setControl(control);

    TimedOverlayManager timedRegionOverlays = new TimedOverlayManager();
    timedRegionOverlays.setMapOverlayManager(mapOverlayManager);
    model.addModelListener(timedRegionOverlays.getPolygonModelListener());

    /**
     * Listeners
     */

    setPage(mainPage);

    StyleInjector.inject(CommuteCalculatorStandardResources.INSTANCE.getCss().getText());
  }
}
