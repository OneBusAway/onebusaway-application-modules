package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public interface ShapePointService {

  /**
   * @param shapeId the target shape id
   * @return the set of shape points for the specified shape id
   */
  public ShapePoints getShapePointsForShapeId(AgencyAndId shapeId);
}
