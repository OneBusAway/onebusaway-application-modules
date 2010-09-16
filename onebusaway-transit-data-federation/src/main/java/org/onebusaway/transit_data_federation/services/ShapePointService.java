package org.onebusaway.transit_data_federation.services;

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
