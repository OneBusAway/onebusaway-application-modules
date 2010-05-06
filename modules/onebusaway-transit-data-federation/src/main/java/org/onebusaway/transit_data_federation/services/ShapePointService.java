package org.onebusaway.transit_data_federation.services;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;

public interface ShapePointService {
  public ShapePoints getShapePointsForShapeId(AgencyAndId shapeId);
}
