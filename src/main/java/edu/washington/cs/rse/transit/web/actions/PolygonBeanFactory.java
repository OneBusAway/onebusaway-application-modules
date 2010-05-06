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
package edu.washington.cs.rse.transit.web.actions;

import java.util.List;


import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;
import edu.washington.cs.rse.transit.web.oba.common.client.model.PolygonBean;

public class PolygonBeanFactory {
    public static PolygonBean create(List<IGeoPoint> points) {

        double[] lat = new double[points.size()];
        double[] lon = new double[points.size()];

        int index = 0;

        for (IGeoPoint gp : points) {
            CoordinatePoint p = gp.getCoordinates();
            lat[index] = p.getLat();
            lon[index] = p.getLon();
            index++;
        }

        PolygonBean pb = new PolygonBean();
        pb.setLat(lat);
        pb.setLon(lon);
        return pb;
    }
}
