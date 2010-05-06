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
package edu.washington.cs.rse.transit.web.oba.standard.client.pages;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Icon;

import edu.washington.cs.rse.transit.web.oba.standard.client.resources.OneBusAwayStandardResources;

public class StopIconFactory {

    private static OneBusAwayStandardResources r = OneBusAwayStandardResources.INSTANCE;

    private static Map<String, String> _directionIcons = new HashMap<String, String>();

    static {

        _directionIcons.put("N", r.getImageNorth().getUrl());
        _directionIcons.put("NE", r.getImageNorthEast().getUrl());
        _directionIcons.put("E", r.getImageEast().getUrl());
        _directionIcons.put("SE", r.getImageSouthEast().getUrl());
        _directionIcons.put("S", r.getImageSouth().getUrl());
        _directionIcons.put("SW", r.getImageSouthWest().getUrl());
        _directionIcons.put("W", r.getImageWest().getUrl());
        _directionIcons.put("NW", r.getImageNorthWest().getUrl());
    }

    public static Icon getRouteStartIcon() {
        return getRouteIcon(r.getImageRouteStart().getUrl());
    }

    public static Icon getRouteEndIcon() {
        return getRouteIcon(r.getImageRouteEnd().getUrl());
    }

    private static Icon getRouteIcon(String url) {
        Icon icon = new Icon();
        icon.setImageURL(url);
        icon.setIconSize(new Size(20, 34));
        icon.setIconAnchor(new Point(10, 34));
        return icon;
    }

    public static Icon getFarStopIcon() {
        Icon icon = new Icon();
        icon.setImageURL(r.getImageFarStop().getUrl());
        icon.setIconSize(new Size(5, 5));
        icon.setIconAnchor(new Point(3, 3));
        return icon;
    }

    public static Icon getMiddleStopIcon() {
        Icon icon = new Icon();
        icon.setImageURL(r.getImageMiddleStop().getUrl());
        icon.setIconSize(new Size(9, 9));
        icon.setIconAnchor(new Point(5, 5));
        return icon;
    }

    public static Icon getIconForDirection(String direction, boolean isSelected) {

        Icon icon = new Icon();
        icon.setImageURL(_directionIcons.get(direction));
        icon.setIconSize(new Size(21, 21));
        icon.setIconAnchor(new Point(11, 11));
        icon.setInfoWindowAnchor(new Point(11, 11));
        if (isSelected) {
            icon.setShadowURL(r.getImageSelectedStop().getUrl());
            icon.setShadowSize(new Size(22, 21));
        }
        return icon;
    }
}
