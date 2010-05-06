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
package edu.washington.cs.rse.transit.common.spatial;

import com.vividsolutions.jts.geom.Coordinate;

import edu.washington.cs.rse.geospatial.ICoordinateProjection;
import edu.washington.cs.rse.geospatial.IGeoPoint;
import edu.washington.cs.rse.geospatial.latlon.CoordinatePoint;

import org.apache.commons.digester.Rule;

import java.util.ArrayList;
import java.util.List;

public class CoordinatesRule extends Rule {

    private ICoordinateProjection _projection;

    public CoordinatesRule(ICoordinateProjection projection) {
        _projection = projection;
    }

    @Override
    public void body(String namespace, String name, String text) throws Exception {
        super.body(namespace, name, text);

        text = text.trim();
        String[] pointTokens = text.split("\\s+");
        Coordinate[] coordinates = new Coordinate[pointTokens.length];

        List<CoordinatePoint> points = new ArrayList<CoordinatePoint>(pointTokens.length);

        for (String pointToken : pointTokens ) {

            String[] tokens = pointToken.split(",");

            if (tokens.length != 2 && tokens.length != 3)
                throw new IllegalStateException("invalid coordinates: " + pointToken);

            double lon = Double.parseDouble(tokens[0]);
            double lat = Double.parseDouble(tokens[1]);
            double z = 0;
            if (tokens.length == 3)
                z = Double.parseDouble(tokens[2]);
            points.add(new CoordinatePoint(lat, lon, z));
        }

        List<IGeoPoint> gps = _projection.forward(points, new ArrayList<IGeoPoint>(points.size()), points.size());

        for (int i = 0; i < gps.size(); i++) {
            IGeoPoint gp = gps.get(i);
            coordinates[i] = new Coordinate(gp.getX(), gp.getY(), gp.getZ());
        }

        HasCoordinates hasCoordiantes = (HasCoordinates) digester.peek();
        hasCoordiantes.setCoordinates(coordinates);
    }
}
