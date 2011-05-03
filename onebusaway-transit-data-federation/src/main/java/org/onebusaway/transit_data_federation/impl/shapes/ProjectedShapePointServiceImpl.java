package org.onebusaway.transit_data_federation.impl.shapes;

import java.util.List;

import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.model.ShapePoints;
import org.onebusaway.transit_data_federation.services.shapes.ProjectedShapePointService;
import org.onebusaway.transit_data_federation.services.shapes.ShapePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ProjectedShapePointServiceImpl implements ProjectedShapePointService {

  private ShapePointService _shapePointService;

  private ShapePointsLibrary _shapePointsLibrary;

  @Autowired
  public void setShapePointService(ShapePointService shapePointService) {
    _shapePointService = shapePointService;
  }

  @Autowired
  public void setShapePointsLibrary(ShapePointsLibrary shapePointsLibrary) {
    _shapePointsLibrary = shapePointsLibrary;
  }

  @Cacheable
  @Override
  public T2<List<XYPoint>, double[]> getProjectedShapePoints(
      List<AgencyAndId> shapeIds, int utmZoneId) {

    ShapePoints shapePoints = _shapePointService.getShapePointsForShapeIds(shapeIds);

    if (shapePoints == null || shapePoints.isEmpty())
      return null;

    UTMProjection projection = new UTMProjection(utmZoneId);

    List<XYPoint> projected = _shapePointsLibrary.getProjectedShapePoints(
        shapePoints, projection);

    return Tuples.tuple(projected, shapePoints.getDistTraveled());
  }
}

