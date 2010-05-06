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

import com.vividsolutions.jts.geom.Geometry;

public class HasGeometries implements IHasGeometry {

    private List<Geometry> _geometies = new ArrayList<Geometry>();

    public List<Geometry> getGeometries() {
        return _geometies;
    }

    /***************************************************************************
     * {@link IHasGeometry} Interface
     **************************************************************************/

    public void setGeometry(Geometry geometry) {
        _geometies.add(geometry);
    }

}
