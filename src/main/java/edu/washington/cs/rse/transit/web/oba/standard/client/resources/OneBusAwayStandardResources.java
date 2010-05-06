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
package edu.washington.cs.rse.transit.web.oba.standard.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.libideas.resources.client.CssResource;
import com.google.gwt.libideas.resources.client.DataResource;
import com.google.gwt.libideas.resources.client.ImageResource;
import com.google.gwt.libideas.resources.client.ImmutableResourceBundle;

public interface OneBusAwayStandardResources extends ImmutableResourceBundle {

    public static OneBusAwayStandardResources INSTANCE = GWT.create(OneBusAwayStandardResources.class);

    @Resource("Bus.png")
    public DataResource getImageBus();

    @Resource("Marker.png")
    public DataResource getImageMarker();

    /***************************************************************************
     * Directional Stop Icons
     **************************************************************************/

    @Resource("North.png")
    public DataResource getImageNorth();

    @Resource("NorthEast.png")
    public DataResource getImageNorthEast();

    @Resource("East.png")
    public DataResource getImageEast();

    @Resource("SouthEast.png")
    public DataResource getImageSouthEast();

    @Resource("South.png")
    public DataResource getImageSouth();

    @Resource("SouthWest.png")
    public DataResource getImageSouthWest();

    @Resource("West.png")
    public DataResource getImageWest();

    @Resource("NorthWest.png")
    public DataResource getImageNorthWest();

    /***************************************************************************
     * Stops
     **************************************************************************/

    @Resource("FarStop.png")
    public DataResource getImageFarStop();

    @Resource("MiddleStop.png")
    public DataResource getImageMiddleStop();

    @Resource("SelectedStop.png")
    public DataResource getImageSelectedStop();

    /***************************************************************************
     * Routes
     **************************************************************************/

    @Resource("RouteStart.png")
    public DataResource getImageRouteStart();

    @Resource("RouteEnd.png")
    public DataResource getImageRouteEnd();

    /***************************************************************************
     * Other
     **************************************************************************/

    @Resource("StopIdentification.jpg")
    public ImageResource getImageStopIdentification();

    @Resource("OneBusAway.css")
    public CssResource getCSS();
}
