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

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;

import edu.washington.cs.rse.geospatial.ICoordinateProjection;

public class KMLRuleSet implements RuleSet {
    
    private ICoordinateProjection _projection;

    public KMLRuleSet(ICoordinateProjection projection) {
        _projection = projection;
    }

    public String getNamespaceURI() {
        return null;
    }

    public void addRuleInstances(Digester digester) {

        digester.addRule("*/MultiGeometry", new MultiGeometryRule());

        digester.addRule("*/LinearRing", new LinearRingRule());
        digester.addRule("*/LinearRing/coordinates", new CoordinatesRule(_projection));

        digester.addRule("*/Polygon", new PolygonRule());
        digester.addRule("*/Polygon/outerBoundaryIs", new PolygonOuterBoundaryRule());
        digester.addRule("*/Polygon/innerBoundaryIs", new PolygonInnerBoundaryRule());

    }
}
