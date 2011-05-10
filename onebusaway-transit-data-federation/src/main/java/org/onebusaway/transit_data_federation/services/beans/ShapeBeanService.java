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
