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

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.LinearRing;

public class PolygonImpl {

    private LinearRing _outerBoundary;

    private List<LinearRing> _innerBoundaries = new ArrayList<LinearRing>();

    public void setOuterBoundary(LinearRing outerBoundary) {
        _outerBoundary = outerBoundary;
    }

    public LinearRing getOuterBoundary() {
        return _outerBoundary;
    }

    public void addInnerBoundary(LinearRing innerBoundary) {
        _innerBoundaries.add(innerBoundary);
    }

    public List<LinearRing> getInnerBoundaries() {
        return _innerBoundaries;
    }
}
