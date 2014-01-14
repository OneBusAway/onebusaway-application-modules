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
package org.onebusaway.transit_data_federation.services.shapes;

import java.util.List;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.transit_data_federation.model.ShapePoints;

/**
 * Service for looking up {@link ShapePoint}
 * 
 * @author bdferris
 * 
 * @see ShapePoint
 * @see ShapePoints
 */
public interface ShapePointService {

  /**
   * @param shapeId the target shape id
   * @return the set of shape points for the specified shape id, or null if the
   *         shape was not found
   */
  public ShapePoints getShapePointsForShapeId(AgencyAndId shapeId);
  
  /**
   * @param shapeIds the target shape ids in order
   * @return the set of shape points for the specified shape ids
   * @throws IllegalStateException if a shape id could not be found
   */
  public ShapePoints getShapePointsForShapeIds(List<AgencyAndId> shapeIds);
}
