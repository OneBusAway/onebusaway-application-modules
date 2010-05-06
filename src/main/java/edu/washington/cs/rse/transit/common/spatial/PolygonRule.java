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

import java.util.List;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonRule extends Rule {

    private static GeometryFactory _factory = new GeometryFactory();

    @Override
    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        super.begin(namespace, name, attributes);
        digester.push(new PolygonImpl());
    }

    @Override
    public void end(String namespace, String name) throws Exception {
        PolygonImpl impl = (PolygonImpl) digester.pop();
        LinearRing outer = impl.getOuterBoundary();
        List<LinearRing> inner = impl.getInnerBoundaries();
        Polygon poly = _factory.createPolygon(outer, inner.toArray(new LinearRing[inner.size()]));

        IHasGeometry g = (IHasGeometry) digester.peek();
        g.setGeometry(poly);
    }
}
