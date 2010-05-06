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
package org.onebusaway.oba.web.commute.client;

import org.onebusaway.common.web.common.client.AbstractSinglePageApplication;
import org.onebusaway.common.web.common.client.MapOverlayManager;
import org.onebusaway.oba.web.common.client.control.TimedRegionOverlayManager;
import org.onebusaway.oba.web.common.client.model.TimedRegionModel;
import org.onebusaway.oba.web.commute.client.control.ControlImpl;
import org.onebusaway.oba.web.commute.client.resources.CommuteCalculatorStandardResources;
import org.onebusaway.oba.web.commute.client.view.MainPage;
import org.onebusaway.oba.web.commute.client.view.SearchWidget;

import com.google.gwt.libideas.client.StyleInjector;
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

    TimedRegionModel model = new TimedRegionModel();

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

    TimedRegionOverlayManager timedRegionOverlays = new TimedRegionOverlayManager();
    timedRegionOverlays.setMapOverlayManager(mapOverlayManager);
    model.addModelListener(timedRegionOverlays);

    /**
     * Listeners
     */

    setPage(mainPage);

    StyleInjector.injectStylesheet(CommuteCalculatorStandardResources.INSTANCE.getCss().getText());
  }
}
