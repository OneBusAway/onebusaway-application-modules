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
/**
 * 
 */
package org.onebusaway.where.web.common.client.view.constraints;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;

import org.onebusaway.common.web.common.client.context.Context;


public class LocationConstraint extends AbstractConstraint {

    private LatLng _location;

    private int _accuracy;

    public LocationConstraint(LatLng location, int accuracy) {
        _location = location;
        _accuracy = accuracy;
    }

    public void update(Context context) {

        _map.setCenter(_location, _accuracy);
        Marker m = new Marker(_location);
        _map.addOverlay(m);

        double lat = _location.getLatitude();
        double lon = _location.getLongitude();
        _service.getStopsByLocationAndAccuracy(lat, lon, _accuracy, _stopsHandler);
    }

}