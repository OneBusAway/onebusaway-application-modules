package org.onebusaway.transit_data_federation.services.shapes;

import java.util.List;

import org.onebusaway.collections.tuple.T2;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.gtfs.model.AgencyAndId;

public interface ProjectedShapePointService {
  public T2<List<XYPoint>, double[]> getProjectedShapePoints(
      List<AgencyAndId> shapeIds, int utmZoneId);
}
