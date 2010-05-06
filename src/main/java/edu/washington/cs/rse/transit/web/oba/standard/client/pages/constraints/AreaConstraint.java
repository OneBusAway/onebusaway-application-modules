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
package edu.washington.cs.rse.transit.web.oba.standard.client.pages.constraints;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;

import edu.washington.cs.rse.transit.web.oba.common.client.Context;

public class AreaConstraint extends AbstractConstraint {

    private LatLngBounds _bounds;

    public AreaConstraint(LatLngBounds bounds) {
        _bounds = bounds;
    }

    public void update(Context context) {

        int zoom = _map.getBoundsZoomLevel(_bounds);
        _map.setCenter(_bounds.getCenter(), zoom);

        LatLng p1 = _bounds.getNorthEast();
        LatLng p2 = _bounds.getSouthWest();
        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        double lat2 = p2.getLatitude();
        double lon2 = p2.getLongitude();

        _service.getStopsByBounds(lat1, lon1, lat2, lon2, _stopsHandler);
    }

}