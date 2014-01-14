/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.geospatial.services;

import java.util.Collection;

import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.XYPoint;

public interface ICoordinateProjection {
    
    public XYPoint forward(CoordinatePoint point);
    
    public <T extends Collection<XYPoint>> T forward(Iterable<CoordinatePoint> source, T dest, int size);

    public CoordinatePoint reverse(XYPoint point);
    
    public <T extends Collection<CoordinatePoint>> T reverse(Iterable<XYPoint> source, T dest, int size);
}
