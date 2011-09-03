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
package org.onebusaway.transit_data_federation.services.beans;

import java.util.Collection;
import java.util.List;

import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.transit_data.model.ListBean;

/**
 * Retrieve {@list ShapePoint} points as {@link EncodedPolylineBean} polyline
 * representations.
 * 
 * @author bdferris
 * @see ShapePoint
 * @see EncodedPolylineBean
 */
public interface ShapeBeanService {

  /**
   * @param shapeId see {@link ShapePoint#getShapeId()}
   * @return the shape points with the specified shapeId as an encoded polyline
   */
  public EncodedPolylineBean getPolylineForShapeId(AgencyAndId shapeId);

  /**
   * While a particular route or set of trips may have a number of distinct
   * shape ids, there is often overlap among the shapes. Here we compute the
   * minimal set of polylines that would be needed to display the shapes without
   * overlapping segments.
   * 
   * @param shapeIds see {@link ShapePoint#getShapeId()}
   * @return the merged minimum spanning tree representation of all the
   *         polylines from the specified set of shape id
   */
  public List<EncodedPolylineBean> getMergedPolylinesForShapeIds(
      Collection<AgencyAndId> shapeIds);

  /**
   * Return the list of all shape ids associated with the specified agency.
   * 
   * @param agencyId
   * @return the list of all shape ids
   */
  public ListBean<String> getShapeIdsForAgencyId(String agencyId);
}
